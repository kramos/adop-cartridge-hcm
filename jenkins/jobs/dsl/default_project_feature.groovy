// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def hcmProjRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_CreateProject"
def defProjFeature = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_DefaultFeatures"

// Jobs
def createProject = freeStyleJob(projectFolderName + "/Default_Project_Feature")
def enableDefaultFeature = freeStyleJob(projectFolderName + "/Enable_Default_Feature")
def deployDefaultFeature = freeStyleJob(projectFolderName + "/Deploy_Default_Feature")
def defaultTemplateCreate = freeStyleJob(projectFolderName + "/Default_Template_Create")

// Views
def pipelineView_5 = buildPipelineView(projectFolderName + "/Default_Project_Feature")

pipelineView_5.with{
    title('Default_Project_Feature')
    displayedBuilds(5)
    selectedJob(projectFolderName + "/Enable_Default_Feature")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

enableDefaultFeature.with{
	description("This downloads and enables the default features for a project.")
	wrappers {
		preBuildCleanup()
		sshAgent("adop-jenkins-master")
	}
	steps {
		shell ('''#!/bin/bash
			      wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/template/enabledefaultfeature/SampleTestData.xlsx 
			''')
	}
	publishers{
		downstreamParameterized{
		  trigger(projectFolderName + "/Deploy_Default_Feature"){
			condition("SUCCESS")
			 parameters{
			  predefinedProp("PARENT_BUILD",'${PARENT_BUILD}')
			}
		  }
		}
	}
}

deployDefaultFeature.with{
	description("This downloads and enables the default features for a project.")
	parameters{
		stringParam("PARENT_BUILD","","Parent build name")
	}
	wrappers {
		preBuildCleanup()
		sshAgent("adop-jenkins-master")
	}
	
	scm{
		git{
		  remote{
			url(defProjFeature)
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
		  goals('-P selenium-projectname-regression-test clean test')
		  mavenInstallation("ADOP Maven")
		}
	}
	
   publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/Default_Template_Create"){
        condition("SUCCESS")
		parameters{
          predefinedProp("PARENT_BUILD",'${PARENT_BUILD}')
        }
      }
    }
  }
}

defaultTemplateCreate.with{
	description("This downloads and enables the default features for a project.")
	parameters{
		stringParam("PARENT_BUILD","","Parent build name")
	}
	wrappers {
		preBuildCleanup()
		sshAgent("adop-jenkins-master")
	}
	steps {
		shell ('''#!/bin/bash
			      wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/template/SampleTestData.xlsx 
			''')
	}
	publishers{
		downstreamParameterized{
		  trigger(projectFolderName + "/Default_Project_Feature"){
			condition("SUCCESS")
			 parameters{
			  predefinedProp("PARENT_BUILD",'${PARENT_BUILD}')
			}
		  }
		}
	}
}


createProject.with{
  description("This job creates a new project in the Oracle HCM Application")
  scm{
    git{
      remote{
        url(hcmProjRepoUrl)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  steps {
    maven{
	  rootPOM('hcmselenium/pom.xml')
      goals('-P selenium-projectname-regression-test clean test')
      mavenInstallation("ADOP Maven")
	}
  }
}



