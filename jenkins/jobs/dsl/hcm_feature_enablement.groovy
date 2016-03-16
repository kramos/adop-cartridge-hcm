def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def fe_FolderName = projectFolderName + "/HCM_Feature_Enablement"
def wd_FolderName = fe_FolderName + "/Enable_Workforce_Development"
def mgs_FolderName = wd_FolderName + "/Manage_Goal_Setting"

def hcmConfRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Configurations"
def hcmAppRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Selenium"
def hcmCreateProjRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_CreateProject"
def hcmWorkGoalRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_WorkforceGoals"
def hcmCdgRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_CareerDevelopmentGoals"

def retrieveConfig = freeStyleJob(wd_FolderName + "/Retrieve_Configuration")
def enableFeature = freeStyleJob(wd_FolderName + "/Enable_Feature")
def createProject = freeStyleJob(wd_FolderName + "/Create_Project")
def manageProject = freeStyleJob(wd_FolderName + "/Manage_Project")
def validate = freeStyleJob(wd_FolderName + "/Validate")

def retrieveConfigGoal = freeStyleJob(mgs_FolderName + "/Retrieve_Configuration")

def workforceDevPipeline = buildPipelineView(wd_FolderName + "/Enable_Workforce_Development")

workforceDevPipeline.with{
    title('Enable_Workforce_Development')
    displayedBuilds(5)
    selectedJob(wd_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file that will be used as a template for managing department to the Oracle HCM Application")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmConfRepoUrl)
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
        shell('''
            #!/bin/bash

            cd /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/Build
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

            cp /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Feature_Enablement/jobs/Enable_Workforce_Development/jobs/Retrieve_Configuration/workspace/SampleTestData.xlsx .
        ''')
    }
  publishers{
    downstreamParameterized{
      trigger(wd_FolderName + "/Enable_Feature"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

retrieveConfigGoal.with{
    description("This retrieves the configuration file that will be used as a template for managing department to the Oracle HCM Application")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmConfRepoUrl)
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
        shell('''
            #!/bin/bash

            cd /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/Build
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

            cp /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Feature_Enablement/jobs/Enable_Workforce_Development/jobs/Manage_Goal_Setting/jobs/Retrieve_Configuration/workspace/SampleTestData.xlsx .
        ''')
    }
  publishers{
    downstreamParameterized{
      trigger(wd_FolderName + "/Manage_Project"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

enableFeature.with{
    description("This enables the workforce development feature for Oracle HCM implementation project.")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmAppRepoUrl)
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
      trigger(wd_FolderName + "/Create_Project"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

createProject.with{
    description("This enables the workforce development feature for Oracle HCM implementation project.")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmCreateProjRepoUrl)
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
	  rootPOM('hcmselenium/pom.xml')
      goals('-P selenium-projectname-regression-test clean test')
      mavenInstallation("ADOP Maven")
	}
  }
  publishers{
    downstreamParameterized{
      trigger(wd_FolderName + "/Manage_Project"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

manageProject.with{
    description("This job enables the feature in the Oracle HCM project.")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmWorkGoalRepoUrl)
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
      trigger(wd_FolderName + "/Validate"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

validate.with{
    description("This job applies enabled feature from the project to the HCM application.")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmCdgRepoUrl)
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
