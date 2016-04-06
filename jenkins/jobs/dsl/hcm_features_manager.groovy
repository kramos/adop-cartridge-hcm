// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def md_FolderName = hfm_FolderName + "/Manage_Department"
def cd_FolderName = hfm_FolderName + "/Create_Department"
def pm_FolderName = hfm_FolderName + "/Person_Management"

// Repositories
def hcmManDepConRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDConfiguration"
def hcmManDepAppRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDApplication"
def hcmEmpManAppRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDPersonManagement"

// Jobs
def retrieveConfig = freeStyleJob(md_FolderName + "/Retrieve_Configuration")
def createDepartment = freeStyleJob(md_FolderName + "/Create_Department")
def employeeManagement = freeStyleJob(md_FolderName + "/Employee_Management")
def createIssue = freeStyleJob(md_FolderName + "/Create_Issue")
def retrieveConfig_2 = freeStyleJob(cd_FolderName + "/Retrieve_Configuration")
def createDepartment_2 = freeStyleJob(cd_FolderName + "/Create_Department")
def retrieveConfig_3 = freeStyleJob(pm_FolderName + "/Retrieve_Configuration")

// Pipeline
def manageDepartment_pipeline = buildPipelineView(md_FolderName + "/Manage_Department")
def createDepartment_pipeline = buildPipelineView(cd_FolderName + "/Create_Department")
def personManagement_pipeline = buildPipelineView(pm_FolderName + "/Person_Management")

manageDepartment_pipeline.with{
    title('Manage_Department')
    displayedBuilds(5)
    selectedJob(md_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

createDepartment_pipeline.with {
 title('Manage_Department')
    displayedBuilds(5)
    selectedJob(cd_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

personManagement_pipeline.with {
 title('Manage_Department')
    displayedBuilds(5)
    selectedJob(pm_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
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
    description("This create a Departmant in Oracle HCM Application")
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
    steps {
        maven{
          rootPOM('pom.xml')
          goals('-P selenium-projectname-regression-test clean test')
          mavenInstallation("ADOP Maven")
        }
    }
	publishers{
    downstreamParameterized{
      trigger(md_FolderName + "/Employee_Management"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}


employeeManagement.with{
	description("this job assigns Department to an Employee in the HCM Application")
	wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmEmpManAppRepo)
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
      trigger(md_FolderName + "/Employee_Management"){
        condition("FAILED")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

createIssue.with {
description("this jobs automatically log a ticket in jira whenever an issue has encountered")
	parameters{
        stringParam("ISSUE_ASSIGNEE","suvra.roy","")
		stringParam("ISSUE_REPORTER","john.smith","")
		stringParam("JIRA_USERNAME","neha.takkar","")
		stringParam("JIRA_PASSWORD","Password01","")
		stringParam("PARENT_BUILD","","Parent Build Name")
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
	    shell ('''
		#!/bin/bash
		
		ISSUE="{\"fields\": {\"project\": {\"key\": \"SP\"},\"issuetype\": {\"name\":\"Bug\"},\"assignee\": {\"name\":\"$ISSUE_ASSIGNEE\"},\"reporter\": {\"name\":\"$ISSUE_REPORTER\"},\"summary\""


		if [ -f $WORKSPACE/issue.json ]
		then
		rm -f issue.json
		fi

		touch issue.json
		echo $ISSUE >> issue.json

		echo ":\"ERROR INVALID EMPLOYEE NAME\",\"description\":\"Unable to find employee. Please check your configuration for invalid values.\"}}" >> issue.json


		curl -u $JIRA_USERNAME:$JIRA_PASSWORD -o result.xml -X POST -H "Content-Type: application/json" -H "Accept: application/json" --data-binary @$WORKSPACE/issue.json http://52.49.28.75/jira/rest/api/2/issue -v -s

		cat result.xml
		''')
	}
}

retrieveConfig_2.with{
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
    steps {
	    shell ('''
		#!/bin/bash
		cd /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Manage_Department/jobs/Retrieve_Configuration
		 if [ -d workspace ]
          then
           if [ -f workspace/DepartmentCreation.xlsx ]
            then
              rm -f workspace/DepartmentCreation.xlsx
           fi
         else
          mkdir workspace
         fi
        cd workspace
        cp /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Create_Department/jobs/Retrieve_Configuration/workspace/DepartmentCreation.xlsx .
		''')
	}
  publishers{
    downstreamParameterized{
      trigger(pm_FolderName + "/Create_Department"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

createDepartment_2.with{
    description("This create a Departmant in Oracle HCM Application")
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
    steps {
        maven{
          rootPOM('pom.xml')
          goals('-P selenium-projectname-regression-test clean test')
          mavenInstallation("ADOP Maven")
        }
    }
}

retrieveConfig_3.with{
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
    steps {
	    shell ('''
		#!/bin/bash
		cd /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Manage_Department/jobs/Retrieve_Configuration
		 if [ -d workspace ]
          then
           if [ -f workspace/DepartmentCreation.xlsx ]
            then
              rm -f workspace/DepartmentCreation.xlsx
           fi
         else
          mkdir workspace
         fi
        cd workspace
        cp /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Person_Management/jobs/Retrieve_Configuration/workspace/DepartmentCreation.xlsx .
		''')
	}
  publishers{
    downstreamParameterized{
      trigger(md_FolderName + "/Employee_Management"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}


