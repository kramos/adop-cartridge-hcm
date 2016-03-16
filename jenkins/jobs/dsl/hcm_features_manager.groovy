// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def md_FolderName = hfm_FolderName + "/Manage_Department"

// Repositories
def hcmManDepConRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDConfiguration"
def hcmManDepAppRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDApplication"

// Jobs
def retrieveConfig = freeStyleJob(md_FolderName + "/Retrieve_Configuration")
def createDepartment = freeStyleJob(md_FolderName + "/Create_Department")

// Pipeline
def manageDepartment = buildPipelineView(md_FolderName + "/Department_Creation")

manageDepartment.with{
    title('Manage_Department')
    displayedBuilds(5)
    selectedJob(md_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

retrieveConfig.with{
    description("This retrieves the configuration file that will be used as a template for managing department to the Oracle HCM Application")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmManDepConRepo)
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
      trigger(md_FolderName + "/Create_Department"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

createDepartment.with{
    description("This retrieves the configuration file that will be used as a template for managing department to the Oracle HCM Application")
    wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmManDepAppRepo)
                credentials("adop-jenkins-master")
            }
            branch("*/master")
        }
    }
    environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
    }
}