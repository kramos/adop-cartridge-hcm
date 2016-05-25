// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def pertask_FolderName = projectFolderName + "/HCM-Core_per_Task"
def jobsfam_FolderName = pertask_FolderName + "/Manage_Job_Families"

// Repositories
def hcmCoreConfig = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM-Core_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(jobsfam_FolderName + "/Retrieve_Configuration")
def jobsfam = freeStyleJob(jobsfam_FolderName + "/Manage_Job_Families")

// Pipeline
def jobsfam_pipeline = buildPipelineView(jobsfam_FolderName + "/Manage_Job_Families")

jobsfam_pipeline.with{
    title('Manage Job Families')
    displayedBuilds(5)
    selectedJob(jobsfam_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  authenticationToken('TWFuYWdlSm9iRmFtaWxpZXM=')
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
      trigger(jobsfam_FolderName + "/Manage_Job_Families"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

jobsfam.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Job_Families/jobs/Manage_Job_Families/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Job Families" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Job_Families/jobs/Retrieve_Configuration/workspace
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