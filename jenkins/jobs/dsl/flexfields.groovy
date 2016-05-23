// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def pertask_FolderName = projectFolderName + "/HCM-Core_per_Task"
def orgstruct_FolderName = pertask_FolderName + "/Manage_Organization_Structure_Descriptive_Flexfields"

// Repositories
def hcmCoreConfig = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM-Core_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(orgstruct_FolderName + "/Retrieve_Configuration")
def flexfields = freeStyleJob(orgstruct_FolderName + "/Manage_Organization_Structure_Descriptive_Flexfields")

// Pipeline
def flexfields_pipeline = buildPipelineView(orgstruct_FolderName + "/Manage_Organization_Structure_Descriptive_Flexfields")

flexfields_pipeline.with{
    title('Manage Organization Structure Descriptive Flexfields')
    displayedBuilds(5)
    selectedJob(orgstruct_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  authenticationToken('TWFuYWdlT3JnYW5pemF0aW9uU3RydWN0dXJlRGVzY3JpcHRpdmVGbGV4ZmllbGRz')
  scm{
    git{
      remote{
        url(hcmCoreConfig)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  publishers{
    downstreamParameterized{
      trigger(orgstruct_FolderName + "/Manage_Organization_Structure_Descriptive_Flexfields"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

flexfields.with{
	parameters{
		stringParam("B","","Build Number")
		stringParam("PARENT_BUILD","","Parent Build Job")
		}
    wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmApp)
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
          goals('clean install')
          mavenInstallation("ADOP Maven")
        }
		
		shell('''#!/bin/bash
#!/bin/bash
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Organization_Structure_Descriptive_Flexfields/jobs/Manage_Organization_Structure_Descriptive_Flexfields/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Organization Structure Descriptive Flexfields" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Organization_Structure_Descriptive_Flexfields/jobs/Retrieve_Configuration/workspace
cd ..
mkdir screenshots 
cd screenshots       
cp -avr $WORKSPACE/target/screenshots/* .
cd ..
rm -rf $WORKSPACE/*
rm -rf $WORKSPACE/.git $WORKSPACE/.settings
rm -f $WORKSPACE/.classpath $WORKSPACE/.project
mv screenshots $WORKSPACE
		''')
    }	
}