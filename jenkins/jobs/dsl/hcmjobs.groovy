// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def HCM_Configuration = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Configurations"
def Oracle_WAC = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/OracleHCM_Java"
def Create_Project = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/OracleHCM_CreateProject"
def OracleHCM_Validation = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/OracleHCM_Validation"
def Pre_defined_defined_2 = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/Predefined_Configuration_2"
def Pre_defined_defined_3 = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/Predefined_Configurations_3"

// Jobs
def build = mavenJob(projectFolderName + "/1_Build")
def buildvalidation = mavenJob(projectFolderName + "/2_BuildValidation")
def deploy = freeStyleJob(projectFolderName + "/3_Deploy")
def validate = freeStyleJob(projectFolderName + "/4_Validate")
def createissue = freeStyleJob(projectFolderName + "/CreateIssue")
def createproject = freeStyleJob(projectFolderName + "/CreateProject")
def template1 = freeStyleJob(projectFolderName + "/Deploy_template_1")
def template2 = freeStyleJob(projectFolderName + "/Deploy_template_2")
def enablecompensation = freeStyleJob(projectFolderName + "/Deploy_template_compensation")


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
        url(Oracle_WAC)
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

buildvalidation.with{
  description("This job builds Java Web Application Controller for validation")
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(OracleHCM_Validation)
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
      trigger(projectFolderName + "/3_Deploy"){
        condition("SUCCESS")
        }
      }
    }
  }


deploy.with{
  description("This job deploy changes to Oracle HCM Application.")
  parameters{
    stringParam("Username","LESTER.LUCENA")
    stringParam("Login_URL","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/HcmFusionHome")
	stringParam("Homepage","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/FuseWelcome")
	stringParam("ConfigurationFile","/var/jenkins_home/jobs/3_Deploy/workspace/config.txt")
	stringParam("DatabaseFile","/var/jenkins_home/jobs/1_Build/workspace/sel-automate/lib/sel.conf")
	stringParam("Selenium_Hub","http://selenium-hub:4444/wd/hub")
	stringParam("Browser","firefox")
  }
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
  steps {
	shell ('''touch lastBuildNumber
			echo $BUILD_NUMBER >> lastBuildNumber

			echo "Converting Excel File to Text File"

			/xlsx2csv/xlsx2csv.py -d ">" /var/jenkins_home/jobs/3_Deploy/workspace/Configurations.xlsx  /var/jenkins_home/jobs/3_Deploy/workspace/config.csv
			mv /var/jenkins_home/jobs/3_Deploy/workspace/config.csv /var/jenkins_home/jobs/3_Deploy/workspace/config.txt

			java -jar /var/jenkins_home/jobs/1_Build/workspace/sel-automate/target/sel-automate-0.0.1-SNAPSHOT.jar -u $Username -p $Password -l $Login_URL -h $Homepage -c $ConfigurationFile -f $DatabaseFile -s $Selenium_Hub -b  $Browser   
			''')
  
  }
  publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/CreateIssue"){
        condition("FAILED")
      }
    }
  }
   publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/4_Validate"){
        condition("SUCCESS")
      }
    }
  }
}

validate.with{
  description("This job check if applied changes where successfull")
  parameters{
	stringParam("Username","LESTER.LUCENA")
    stringParam("Login_URL","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/HcmFusionHome")
	stringParam("Homepage","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/FuseWelcome")
	stringParam("ConfigurationFile","/var/jenkins_home/jobs/3_Deploy/workspace/config.txt")
	stringParam("DatabaseFile","/var/jenkins_home/jobs/1_Build/workspace/sel-automate/lib/sel.conf")
	stringParam("Selenium_Hub","http://selenium-hub:4444/wd/hub")
	stringParam("Browser","firefox")
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
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
  steps {
	shell ('''echo "Converting Excel File to Text File"

			/xlsx2csv/xlsx2csv.py -d ">" /var/jenkins_home/jobs/4_Validate/workspace/Configurations.xlsx  /var/jenkins_home/jobs/4_Validate/workspace/config.csv
			mv /var/jenkins_home/jobs/4_Validate/workspace/config.csv /var/jenkins_home/jobs/4_Validate/workspace/config.txt

			java -jar /var/jenkins_home/jobs/2_BuildValidation/workspace/sel-test/target/sel-test-0.0.1-SNAPSHOT.jar -u $Username -p $Password -l $Login_URL -h $Homepage -c $ConfigurationFile -f $DatabaseFile -s $Selenium_Hub -b  $Browser
			''')
  } 
}
createissue.with{
  description("This job create an issue when deploy failed")
  parameters{
    stringParam("JIRA_USERNAME","john.smith")
    stringParam("JIRA_PASSWORD","Password01")
    stringParam("JIRA_URL","http://localhost:8081/jira")
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

	LOG_PATH=$JENKINS_HOME/jobs/3_Deploy/builds/lastFailedBuild
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

#if grep -i 'Finished: FAILURE' $LOG_PATH/log >> $ISSUE_PATH
#if grep -i 'Finished' $LOG_PATH/log >> $ISSUE_PATH

if grep -i '[ERROR 100] INVALID INPUT:' $LOG_PATH/log
then
        echo ":\"ERROR 100 INVALID INPUT\",\n\t\t\"description\":\"Unable to Locate element. Please check your excel configuration file for invalid values.\"\n\t}\n}" >> $ISSUE_PATH
elif grep -i '[ERROR 101] CANNOT FIND:' $LOG_PATH/log
then
        echo ":\"ERROR 101 CANNOT FIND ID\",\n\t\t\"description\":\"Unable to Locate Element ID. Please check your .conf or configuration file for invalid element ID.\"\n\t}\n}" >> $ISSUE_PATH
fi
cat $ISSUE_PATH
curl -u $JIRA_USERNAME:$JIRA_PASSWORD -o result.xml -X POST -H "Content-Type: application/json" -H "Accept: application/json" --data-binary @$JENKINS_HOME/jobs/CreateIssue/issue.json $JIRA_URL/rest/api/2/issue -v -s
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
  parameters{
    stringParam("Username","LESTER.LUCENA")
    stringParam("Login_URL","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/HcmFusionHome")
	stringParam("Homepage","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/FuseWelcome")
	stringParam("ConfigurationFile","/var/jenkins_home/jobs/Deploy_template_1/workspace/config.txt")
	stringParam("DatabaseFile","/var/jenkins_home/jobs/1_Build/workspace/sel-automate/lib/sel.conf")
	stringParam("Selenium_Hub","http://selenium-hub:4444/wd/hub")
	stringParam("Browser","firefox")
  }
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
    scm{
    git{
      remote{
        url(Pre_defined_defined_2)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  configure { project ->
        (project / 'auth_token').setValue('deploy_template_1')
    }

  steps {
	shell ('''echo "Converting Excel File to Text File"

			/xlsx2csv/xlsx2csv.py -d ">" /var/jenkins_home/jobs/Deploy_template_1/workspace/Configurations.xlsx  /var/jenkins_home/jobs/Deploy_template_1/workspace/config.csv
			mv /var/jenkins_home/jobs/Deploy_template_1/workspace/config.csv /var/jenkins_home/jobs/Deploy_template_1/workspace/config.txt

			java -jar /var/jenkins_home/jobs/1_Build/workspace/sel-automate/target/sel-automate-0.0.1-SNAPSHOT.jar -u $Username -p $Password -l $Login_URL -h $Homepage -c $ConfigurationFile -f $DatabaseFile -s $Selenium_Hub -b  $Browser   
			''')
  
  }
}

template2.with{
  description("This job deploy changes to Oracle HCM Application.")
  parameters{
    stringParam("Username","LESTER.LUCENA")
    stringParam("Login_URL","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/HcmFusionHome")
	stringParam("Homepage","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/FuseWelcome")
	stringParam("ConfigurationFile","/var/jenkins_home/jobs/Deploy_template_2/workspace/config.txt")
	stringParam("DatabaseFile","/var/jenkins_home/jobs/1_Build/workspace/sel-automate/lib/sel.conf")
	stringParam("Selenium_Hub","http://selenium-hub:4444/wd/hub")
	stringParam("Browser","firefox")
  }
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
    scm{
    git{
      remote{
        url(Pre_defined_defined_3)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  configure { project ->
        (project / 'auth_token').setValue('deploy_template_2')
    }

  steps {
	shell ('''echo "Converting Excel File to Text File"

			/xlsx2csv/xlsx2csv.py -d ">" /var/jenkins_home/jobs/Deploy_template_2/workspace/Configurations.xlsx  /var/jenkins_home/jobs/Deploy_template_2/workspace/config.csv
			mv /var/jenkins_home/jobs/Deploy_template_2/workspace/config.csv /var/jenkins_home/jobs/Deploy_template_2/workspace/config.txt

			java -jar /var/jenkins_home/jobs/1_Build/workspace/sel-automate/target/sel-automate-0.0.1-SNAPSHOT.jar -u $Username -p $Password -l $Login_URL -h $Homepage -c $ConfigurationFile -f $DatabaseFile -s $Selenium_Hub -b  $Browser   
			''')
  
  }
}

enablecompensation.with{
  description("This job enable features in Oracle HCM Application.")
  parameters{
    stringParam("Username","LESTER.LUCENA")
    stringParam("Login_URL","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/HcmFusionHome")
	stringParam("Homepage","https://hcm-aufsn4x0cba.oracleoutsourcing.com/hcmCore/faces/FuseWelcome")
	stringParam("ConfigurationFile","/var/jenkins_home/jobs/Deploy_template_compensation/workspace/config.txt")
	stringParam("DatabaseFile","/var/jenkins_home/jobs/1_Build/workspace/sel-automate/lib/sel.conf")
	stringParam("Selenium_Hub","http://selenium-hub:4444/wd/hub")
	stringParam("Browser","firefox")
  }
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
	shell ('''wget https://s3-eu-west-1.amazonaws.com/oracle-hcm/template/Configurations.xlsx

			  echo "Converting Excel File to Text File"

			  /xlsx2csv/xlsx2csv.py -d ">" /var/jenkins_home/jobs/Deploy_template_compensation/workspace/Configurations.xlsx  /var/jenkins_home/jobs/Deploy_template_compensation/workspace/config.csv
			  mv /var/jenkins_home/jobs/Deploy_template_compensation/workspace/config.csv /var/jenkins_home/jobs/Deploy_template_compensation/workspace/config.txt

			  java -jar /var/jenkins_home/jobs/1_Build/workspace/sel-automate/target/sel-automate-0.0.1-SNAPSHOT.jar -u $Username -p $Password -l $Login_URL -h $Homepage -c $ConfigurationFile -f $DatabaseFile -s $Selenium_Hub -b  $Browser   
		  ''') 
  }
}



