// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def pertask_FolderName = projectFolderName + "/HCM-Core_per_Task"
def optionvalues_FolderName = pertask_FolderName + "/Manage_Workforce_Records_Profile_Option_Values"

// Repositories
def hcmCoreConfig = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM-Core_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(optionvalues_FolderName + "/Retrieve_Configuration")
def profileoption = freeStyleJob(optionvalues_FolderName + "/Manage_Workforce_Records_Profile_Option_Values")

// Pipeline
def profileoption_pipeline = buildPipelineView(optionvalues_FolderName + "/Manage_Work_Schedule_Assignment_Administration")

profileoption_pipeline.with{
    title('Manage Workforce Schedule Assignment Administration')
    displayedBuilds(5)
    selectedJob(optionvalues_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  authenticationToken('TWFuYWdlV29ya2ZvcmNlUmVjb3Jkc1Byb2ZpbGVPcHRpb25WYWx1ZXM=')
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
      trigger(optionvalues_FolderName + "/Manage_Workforce_Records_Profile_Option_Values"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

profileoption.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Workforce_Records_Profile_Option_Values/jobs/Manage_Workforce_Records_Profile_Option_Values/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Workforce Records Profile Option Values" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Workforce_Records_Profile_Option_Values/jobs/Retrieve_Configuration/workspace
cd ..
mkdir screenshots 
cd screenshots       
cp -avr $WORKSPACE/target/screenshots/* .
cd ..
rm -rf $WORKSPACE/*
rm -rf $WORKSPACE/.git $WORKSPACE/.settings
rm -f $WORKSPACE/.classpath $WORKSPACE/.project
mv screenshots $WORKSPACE
sed -n -e '/R E P O R T   S U M M A R Y/,/E N D   O F   R E P O R T/ p' $WORKSPACE/../builds/${BUILD_ID}/log > reportsummary.txt

		''')
    }	
}