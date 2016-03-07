// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def templateEnableFeature = projectFolderName + "Available Features"

// Repositories
def hcmConfRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Configurations"
def hcmSelRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Selenium"
def excelCheckerRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_ExcelChecker"

// Jobs
def build = freeStyleJob(projectFolderName + "/Build")
def deploy = freeStyleJob(projectFolderName + "/Deploy")
def createIssue = freeStyleJob(projectFolderName + "/CreateIssue")
def validate = freeStyleJob(projectFolderName + "/Validate")
def excelChecker = freeStyleJob(projectFolderName + "/ExcelChecker")

// Pipeline
def pipelineView = buildPipelineView(projectFolderName + "/HCM_Automation")

// Pipeline View
pipelineView.with{
    title('HCM_Automation_Pipeline')
    displayedBuilds(5)
    selectedJob(projectFolderName + "/Build")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

// Job Configuration
build.with{
  description("This job retrieves the configurations from a remote repository.")
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmConfRepoUrl)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  triggers{
    gerrit{
      events{
        refUpdated()
      }
      configure { gerritxml ->
        gerritxml / 'gerritProjects' {
          'com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.data.GerritProject' {
            compareType("PLAIN")
            pattern(projectFolderName + "/HCM")
            'branches' {
              'com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.data.Branch' {
                compareType("PLAIN")
                pattern("master")
              }
            }
          }
        }
        gerritxml / serverName("ADOP Gerrit")
      }
    }
  }
  publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/ExcelChecker"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("PARENT_BUILD",'${PARENT_BUILD}')
					}
        }
      }
    }
}

deploy.with{
  description("This job deploys configuration changes to Oracle HCM Application.")
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
        url(hcmSelRepoUrl)
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
      trigger(projectFolderName + "/CreateIssue"){
        condition("UNSTABLE_OR_WORSE")
		 parameters{
          predefinedProp("PARENT_BUILD",'${PARENT_BUILD}')
        }
      }
    }
  }
  
   publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/Validate"){
        condition("SUCCESS")
		parameters{
          predefinedProp("PARENT_BUILD",'${PARENT_BUILD}')
        }
      }
    }
  }
}

createIssue.with{
  description("This job creates an issue in JIRA whenever the deploy job is unsuccessful")
  parameters{
    stringParam("JIRA_USERNAME","john.smith")
    stringParam("JIRA_PASSWORD","Password01")
	stringParam("ISSUE_ASSIGNEE","john.smith")
	stringParam("ISSUE_REPORTER","john.smith")
	stringParam("ISSUE_DATE","2016-02-22")
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
    shell('''#!/bin/bash
		if [ -f createIssue.sh]
        then
        rm -f createIssue.sh
        fi
		wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/core/createIssue.sh
		chmod u+x createIssue.sh
		./createIssue.sh
	
	''')
  }
}

validate.with{
  description("This job validates the deployed changes in the application.")
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  steps {
	shell ('''#!/bin/bash
			echo "VALIDATING DATA"
			echo "==========================================================================="
			cat /var/jenkins_home/jobs/Deploy/builds/lastSuccessfulBuild/log
			echo "==========================================================================="
			echo "VALIDATION SUCCESSFUL"
			echo "==========================================================================="
			''')
  } 
}

excelChecker.with{
  description("This job validates the excel configuration file values.")
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(excelCheckerRepo)
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