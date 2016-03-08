// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def enableFolderName = projectFolderName + "/Features_to_Enable";

// Jobs
def enableFeature1 = freeStyleJob(enableFolderName + "/Enable_Workforce_Development")
def enableFeature2 = freeStyleJob(enableFolderName + "/Enable_Compensation_Management")
def enableFeature3 = freeStyleJob(enableFolderName + "/Enable_Workforce_Deployment")

// Pipeline
def enableFeaturePipe1 = buildPipelineView(enableFolderName + "/Enable_Workforce_Development")
def enableFeaturePipe2 = buildPipelineView(enableFolderName + "/Enable_Compensation_Management")
def enableFeaturePipe3 = buildPipelineView(enableFolderName + "/Enable_Workforce_Deployment")

// Views
enableFeaturePipe1.with{
    title('Enable_Workforce_Development')
    displayedBuilds(5)
    selectedJob(enableFolderName + "/Enable_Workforce_Development")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

enableFeaturePipe2.with{
    title('Enable_Compensation_Management')
    displayedBuilds(5)
    selectedJob(enableFolderName + "/Enable_Compensation_Management")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

enableFeaturePipe3.with{
    title('Enable_Workforce_Deployment')
    displayedBuilds(5)
    selectedJob(enableFolderName + "/Enable_Workforce_Deployment")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

// Job Configurations

enableFeature1.with{
  description("This job deploys a set of changes from a template to the Oracle HCM Application.")
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
			cd ../../../../Build
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

enableFeature2.with{
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
			cd ../../../../Build
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

enableFeature3.with{
  description("This job deploys a set of changes from a template to the Oracle HCM Application.")
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
			cd ../../../../Build
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