// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Job
def enableWorkforceDep = freeStyleJob(projectFolderName + "/Features_to_Enable" + "/Enable_Workforce_Deployment")

// Pipeline
def enableWorkforceDepPipe = buildPipelineView(projectFolderName + "/Features_to_Enable" + "/Enable_Workforce_Deployment")

// Configurations
enableWorkforceDepPipe.with{
    title('Enable_Workforce_Deployment')
    displayedBuilds(5)
    selectedJob(projectFolderName + "/Features_to_Enable" + "/Enable_Workforce_Deployment")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

enableWorkforceDep.with{
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
        (project / 'auth_token').setValue('deploy_template_2')
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
			wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/template/pre-defined_template_2/SampleTestData.xlsx
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