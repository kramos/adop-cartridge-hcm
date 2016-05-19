// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def pertask_FolderName = projectFolder + "/HCM-Core_per_Task"
def implementdatarole_FolderName = pertask_FolderName + "/Implement_Data_Role_To_Users"

// Repositories
def hcmCoreConfig = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM-Core_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(implementdatarole_FolderName + "/Retrieve_Configuration")
def applydatarole = freeStyleJob(implementdatarole_FolderName + "/Apply_Data_Role")

// Pipeline
def applydatarole_pipeline = buildPipelineView(implementdatarole_FolderName + "/Implement_Data_Role_To_Users")

createuser_pipeline.with{
    title('Implement Data Role to Users')
    displayedBuilds(5)
    selectedJob(implementdatarole_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
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
      trigger(implementdatarole_FolderName + "/Apply_Data_Role){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

applydatarole.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Implement_Data_Role_To_Users/jobs/Apply_Data_Role/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Provision Roles to Implementation Users" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Implement_Data_Role_To_Users/jobs/Retrieve_Configuration/workspace
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