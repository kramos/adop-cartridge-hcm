// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def pertask_FolderName = projectFolderName + "/HCM-Core_per_Task"
def valueset_FolderName = pertask_FolderName + "/Manage_Value_Sets_for_Global_Human_Resource"

// Repositories
def hcmCoreConfig = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM-Core_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(valueset_FolderName + "/Retrieve_Configuration")
def valueset = freeStyleJob(valueset_FolderName + "/Manage_Value_Sets_for_Global_Human_Resource")

// Pipeline
def valueset_pipeline = buildPipelineView(valueset_FolderName + "/Manage_Value_Sets_for_Global_Human_Resource")

valueset_pipeline.with{
    title('Manage Value Sets for Global Human Resource')
    displayedBuilds(5)
    selectedJob(valueset_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  authenticationToken('VmFsdWVTZXQ=')
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
      trigger(valueset_FolderName + "/Manage_Value_Sets_for_Global_Human_Resource"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

valueset.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Value_Sets_for_Global_Human_Resource/jobs/Manage_Value_Sets_for_Global_Human_Resource/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Value Sets" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM-Core_per_Task/jobs/Manage_Value_Sets_for_Global_Human_Resource/jobs/Retrieve_Configuration/workspace
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