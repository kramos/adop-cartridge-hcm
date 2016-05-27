// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def pertask_FolderName = projectFolderName + "/HCM-Core_per_Task"
def qualifiers_FolderName = pertask_FolderName + "/Manage_Instance_Qualifiers"

// Repositories
def hcmCoreConfig = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM-Core_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(qualifiers_FolderName + "/Retrieve_Configuration")
def instance = freeStyleJob(qualifiers_FolderName + "/Manage_Instance_Qualifiers")

// Pipeline
def instance_pipeline = buildPipelineView(qualifiers_FolderName + "/Manage_Instance_Qualifiers")

instance_pipeline.with{
    title('Manage Instance Qualifiers')
    displayedBuilds(5)
    selectedJob(qualifiers_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  authenticationToken('TWFuYWdlSW5zdGFuY2VRdWFsaWZpZXJz')
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
      trigger(qualifiers_FolderName + "/Manage_Instance_Qualifiers"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

instance.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Instance_Qualifiers/jobs/Manage_Instance_Qualifiers/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Instance Qualifiers" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Instance_Qualifiers/jobs/Retrieve_Configuration/workspace
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