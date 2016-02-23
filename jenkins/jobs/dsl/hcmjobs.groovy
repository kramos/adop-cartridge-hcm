// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def HCM_Configuration = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Configurations"
def HCM_Selenium = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Selenium"
def Oracle_WAC = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/OracleHCM_Java"
def Create_Project = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/OracleHCM_CreateProject"
def OracleHCM_Validation = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/OracleHCM_Validation"
def Pre_defined_defined_2 = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/Predefined_Configuration_2"
def Pre_defined_defined_3 = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/Predefined_Configurations_3"

// Jobs
def build = mavenJob(projectFolderName + "/Build")
def deploy = freeStyleJob(projectFolderName + "/Deploy")
def validate = freeStyleJob(projectFolderName + "/Validate")
def createissue = freeStyleJob(projectFolderName + "/CreateIssue")
def createproject = freeStyleJob(projectFolderName + "/CreateProject")
def template1 = freeStyleJob(projectFolderName + "/DeployTemplate_1")
def template2 = freeStyleJob(projectFolderName + "/DeployTemplate_2")
def enablecompensation = freeStyleJob(projectFolderName + "/DeployTemplate_Compensation")


// Views
def pipelineView = buildPipelineView(projectFolderName + "/HCM_Automation")

pipelineView.with{
    title('HCM_Automation_Pipeline')
    displayedBuilds(5)
    selectedJob(projectFolderName + "/1_Build")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

build.with{
  description("This job builds Java Web Application Controller")
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(HCM_Configuration)
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
            pattern(projectFolderName + "/" + referenceAppgitRepo)
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
      trigger(projectFolderName + "/2_BuildValidation"){
        condition("SUCCESS")
        }
      }
    }
}

deploy.with{
  description("This job deploy changes to Oracle HCM Application.")
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
    scm{
    git{
      remote{
        url(HCM_Selenium)
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
      }
    }
  }
   publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/Validate"){
        condition("SUCCESS")
      }
    }
  }
}

validate.with{
  description("This job check if applied changes where successfull")
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  steps {
	shell ('''
			echo "VALIDATING DATA"
			echo "==========================================================================="
			cat /var/jenkins_home/jobs/Deploy/builds/lastSuccessfulBuild/log
			echo "==========================================================================="
			echo "VALIDATION SUCCESSFUL"
			echo "==========================================================================="
			''')
  } 
}

createissue.with{
  description("This job create an issue when deploy failed")
  parameters{
    stringParam("JIRA_USERNAME","john.smith")
    stringParam("JIRA_PASSWORD","Password01")
    stringParam("JIRA_URL","http://localhost:8082/jira")
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
    shell(''' cd $JENKINS_HOME/jobs/CreateIssue
	
	#!/bin/bash

	LOG_PATH=$JENKINS_HOME/jobs/Deploy/builds/lastUnsuccessfulBuild
ISSUE="{\n
  \t\"fields\": {\n
    \t\t\"project\": {\n
     \t\t\t\"key\": \"HCM\"\n
        \t\t},\n
        \t\t\"issuetype\": {\n
               \t\t\t \"id\":\"10000\"\n
        \t\t},\n
        \t\t\"assignee\": {\n
                \t\t\t\"name\":\"$ISSUE_ASSIGNEE\"\n
        \t\t},\n
        \t\t\"reporter\": {\n
                \t\t\t\"name\":\"$ISSUE_REPORTER\"\n
        \t\t},\n
        \t\t\"duedate\":\"$ISSUE_DATE\",\n
        \t\t\"summary\""
		

ISSUE_PATH=$JENKINS_HOME/jobs/CreateIssue/issue.json

rm -f $ISSUE_PATH
touch $ISSUE_PATH

echo $ISSUE >> $ISSUE_PATH

if grep -i 'AssertionError' $LOG_PATH/log
then
        echo ":\"ERROR INVALID INPUT\",\n\t\t\"description\":\"Unable to locate element. Please check your excel configuration file for invalid values.\"\n\t}\n}" >> $ISSUE_PATH
fi

echo "============================================================================="
echo "LOGGING ISSUE"
echo "============================================================================="
cat $ISSUE_PATH

curl -u $JIRA_USERNAME:$JIRA_PASSWORD -o result.xml -X POST -H "Content-Type: application/json" -H "Accept: application/json" --data-binary @$JENKINS_HOME/jobs/CreateIssue/issue.json $JIRA_URL/rest/api/2/issue -v -s

echo "============================================================================="
echo "ISSUE GENERATED: SEE DETAILS BELOW"
echo "============================================================================="
cat result.xml 
''')
  }
}

createproject.with{
  description("This job automatically create new project in Oracle HCM Application")
  scm{
    git{
      remote{
        url(Create_Project)
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
  description("This job deploy changes to Oracle HCM Application.")
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
	shell ('''
			cd ../../Build/workspace
			rm -f SampleTestData.xlsx
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
  description("This job deploy changes to Oracle HCM Application.")
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
	shell ('''
			cd ../../Build/workspace
			rm -f SampleTestData.xlsx
			wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/template/pre-defined_template_2/SampleTestData.xlsx
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



