// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def enableFolderName = projectFolderName + "/Features_to_Enable";

// Jobs
def enableFeature = freeStyleJob(enableFolderName + "/Enable_Compensation_Management")

//Pipeline
def enableFeaturePipe = buildPipelineView(enableFolderName + "/Enable_Compensation_Management")

//Views
enableFeaturePipe.with{
    title('Enable_Compensation_Management')
    displayedBuilds(5)
    selectedJob(enableFolderName + "/Enable_Compensation_Management")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

enableFeature.with{
  description("This job enables the Compensation Management feature in the Oracle HCM Application.")
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }

  steps {
	shell ('''#!/bin/bash
			cd ../../Build
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
            wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/template/enable_compensation_management/SampleTestData.xlsx
			''')
  }
  publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/Deploy"){
        condition("SUCCESS")
		parameters{
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}