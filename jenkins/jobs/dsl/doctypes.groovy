// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def pertask_FolderName = projectFolderName + "/HCM-Core_per_Task"
def doctype_FolderName = pertask_FolderName + "/Manage_Document_Types"

// Repositories
def hcmCoreConfig = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM-Core_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(assignmentstatus_FolderName + "/Retrieve_Configuration")
def doctype = freeStyleJob(assignmentstatus_FolderName + "/Manage_Document_Types")

// Pipeline
def doctype_pipeline = buildPipelineView(assignmentstatus_FolderName + "/Manage_Document_Types")

doctype_pipeline.with{
    title('Manage Document Types')
    displayedBuilds(5)
    selectedJob(assignmentstatus_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  authenticationToken('TWFuYWdlRG9jdW1lbnRUeXBlcw==')
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
      trigger(assignmentstatus_FolderName + "/Manage_Document_Types"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

doctype.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Document_Types/jobs/Manage_Document_Types/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Document Types" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Document_Types/jobs/Retrieve_Configuration/workspace
cd ..
mkdir screenshots 
cd screenshots       
cp -avr $WORKSPACE/target/screenshots/* .
cd ..
rm -rf $WORKSPACE/*
rm -rf $WORKSPACE/.git $WORKSPACE/.settings
rm -f $WORKSPACE/.classpath $WORKSPACE/.project
mv screenshots $WORKSPACE
sed -n -e '/R E P O R T   S U M M A R Y/,/E N D   O F   R E P O R T/ p' $WORKSPACE/../builds/${BUILD_ID}/log > $WORKSPACE/reportsummary.txt
		''')
    }	
}