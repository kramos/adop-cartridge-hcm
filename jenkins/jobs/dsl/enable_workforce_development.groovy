// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def enableFolder = projectFolderName + "/Features_to_Enable";

// Jobs
def enableWorkforceDev = freeStyleJob(enableFolder + "/Enable_Workforce_Development")

// Pipeline
def enableWorkforceDevPipe = buildPipelineView(enableFolder + "/Enable_Workforce_Development")

// Views
enableWorkforceDevPipe.with{
    title('Enable_Workforce_Development')
    displayedBuilds(5)
    selectedJob(enableFolder + "/Enable_Workforce_Development")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

// Job Configuration
enableWorkforceDev.with{
  description("This job deploys a set of changes from a template to the Oracle HCM Application.")
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  configure { project ->
        (project / 'auth_token').setValue('deploy_template_1')
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
			wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/template/pre-defined_template_1/SampleTestData.xlsx   
			''')
  
  }
  publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/Deploy"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}