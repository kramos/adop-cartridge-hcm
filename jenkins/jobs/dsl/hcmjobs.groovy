// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def hcmConfRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Configurations"
def hcmSelRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Selenium"
def hcmProjRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_CreateProject"

// Jobs
def build = freeStyleJob(projectFolderName + "/Build")
def deploy = freeStyleJob(projectFolderName + "/Deploy")
def validate = freeStyleJob(projectFolderName + "/Validate")
def createIssue = freeStyleJob(projectFolderName + "/CreateIssue")
def createProject = freeStyleJob(projectFolderName + "/CreateProject")
def template1 = freeStyleJob(projectFolderName + "/DeployTemplate_1")
def template2 = freeStyleJob(projectFolderName + "/DeployTemplate_2")
def template3 = freeStyleJob(projectFolderName + "/Enable_Compensation_Management")


// Views
def pipelineView = buildPipelineView(projectFolderName + "/HCM_Automation")

pipelineView.with{
    title('HCM_Automation_Pipeline')
    displayedBuilds(5)
    selectedJob(projectFolderName + "/Build")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

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
      trigger(projectFolderName + "/Deploy"){
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

createIssue.with{
  description("This job creates an issue in JIRA whenever the deploy job is unsuccessful")
  parameters{
    stringParam("JIRA_USERNAME","john.smith")
    stringParam("JIRA_PASSWORD","Password01")
	stringParam("ISSUE_ASSIGNEE","john.smith")
	stringParam("ISSUE_REPORTER","john.smith")
	stringParam("ISSUE_DATE","2016-22-02")
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
		wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/core/createIssue.sh
		chmod u+x createIssue.sh
		./createIssue.sh
	''')
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

template1.with{
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
      }
    }
  }
}

template2.with{
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

template3.with{
  description("This job enables the Compensation Management feature in the Oracle HCM Application.")
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  configure { project ->
        (project / 'auth_token').setValue('deploy_template_compensation')
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



