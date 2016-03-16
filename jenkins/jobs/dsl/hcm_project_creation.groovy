def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def pc_FolderName = projectFolderName + "/HCM_Project_Creation"

def enableFeatureRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_EnableFeature"
def projectCreateRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_ProjectCreation"

def defaultConfig = freeStyleJob(pc_FolderName + "/Default_Configuration")
def enableFeature = freeStyleJob(pc_FolderName + "/Enable_Feature")
def projectCreate = freeStyleJob(pc_FolderName + "/Project_Creation")

def projectCreatePipe = buildPipelineView(pc_FolderName + "/Create_Default_Project")

projectCreatePipe.with{
    title('Create Default Project')
    displayedBuilds(5)
    selectedJob(pc_FolderName + "/Default_Configuration")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

defaultConfig.with{
    description("This retrieves the configuration file that will be used as a template for managing department to the Oracle HCM Application")
    parameters{
        stringParam("HCM_PROJECT","","HCM Project Name")
    }
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  steps {
      shell('''
            #!/bin/bash
			cd /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/Build/
            if [ -d workspace ]
            then
                if [ -f workspace/SampleTestData.xlsx ]
			    then
			       rm -f workspace/SampleTestData.xlsx
			    fi
            else
                mkdir workspace
            fi
			cd workspace
         	wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/template/one-clickdeploy/SampleTestData.xlsx
      ''')
  }
  publishers{
    downstreamParameterized{
      trigger(pc_FolderName + "/Enable_Feature"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
          predefinedProp("HCM_PROJECT", '${HCM_PROJECT}')
        }
      }
    }
  }
}

enableFeature.with{
    description("This enables the feature from a template.")
    parameters{
        stringParam("HCM_PROJECT","","HCM Project Name")
        stringParam("PARENT_BUILD","","")
    }
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(enableFeatureRepo)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  steps {
      maven{
	    rootPOM('pom.xml')
        goals('-P selenium-projectname-regression-test clean test')
        mavenInstallation("ADOP Maven")
      }
  }
  publishers{
    downstreamParameterized{
      trigger(pc_FolderName + "/Project_Creation"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
          predefinedProp("HCM_PROJECT", '${HCM_PROJECT}')
        }
      }
    }
  }
}

projectCreate.with{
    description("This creates the project with the enabled features.")
    parameters{
        stringParam("HCM_PROJECT","","HCM Project Name")
        stringParam("PARENT_BUILD","","")
    }
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(projectCreateRepo)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  steps {
      maven{
	    rootPOM('pom.xml')
        goals('-P selenium-projectname-regression-test clean test')
        mavenInstallation("ADOP Maven")
      }
  }
}