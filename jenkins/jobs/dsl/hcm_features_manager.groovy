// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def md_FolderName = hfm_FolderName + "/Manage_Department"
def cd_FolderName = hfm_FolderName + "/Create_Department"
def pm_FolderName = hfm_FolderName + "/Person_Management"
def cp_FolderName = hfm_FolderName + "/Set_1"

// Repositories
def hcmManDepConRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDConfiguration"
def hcmManDepAppRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDApplication"
def hcmEmpManAppRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDPersonManagement"
def hcmSet1Config = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set1_Config"
def hcmEnableFeatRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set1_EnableFeature"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(md_FolderName + "/Retrieve_Configuration")
def createDepartment = freeStyleJob(md_FolderName + "/Create_Department")
def employeeManagement = freeStyleJob(md_FolderName + "/Employee_Management")
def createIssue = freeStyleJob(md_FolderName + "/Create_Issue")
def retrieveConfig_2 = freeStyleJob(cd_FolderName + "/Retrieve_Configuration")
def createDepartment_2 = freeStyleJob(cd_FolderName + "/Create_Department")
def retrieveConfig_3 = freeStyleJob(pm_FolderName + "/Retrieve_Configuration")
def retrieveConfig_4 = freeStyleJob(cp_FolderName + "/Retrieve_Configuration")
def enablefeature = freeStyleJob(cp_FolderName + "/Enable_Feature")
def createproject = freeStyleJob(cp_FolderName + "/Create_Project")
def managecurrencies = freeStyleJob(cp_FolderName + "/Manage_Currencies")
def createuser = freeStyleJob(cp_FolderName + "/Create_User")
def adddatarole = freeStyleJob(cp_FolderName + "/Add_User_Data_Role")
def applydatarole = freeStyleJob(cp_FolderName + "/Apply_Data_Role")

// Pipeline
def manageDepartment_pipeline = buildPipelineView(md_FolderName + "/Manage_Department")
def createDepartment_pipeline = buildPipelineView(cd_FolderName + "/Create_Department")
def personManagement_pipeline = buildPipelineView(pm_FolderName + "/Person_Management")
def usecase1_pipeline = buildPipelineView(cp_FolderName + "/Set1")

manageDepartment_pipeline.with{
    title('Manage_Department')
    displayedBuilds(5)
    selectedJob(md_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

createDepartment_pipeline.with {
 title('Create_Department')
    displayedBuilds(5)
    selectedJob(cd_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

personManagement_pipeline.with {
 title('Employee_Management')
    displayedBuilds(5)
    selectedJob(pm_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

usecase1_pipeline.with{
    title('Set 1')
    displayedBuilds(5)
    selectedJob(cp_FolderName + "/Retrieve_Configuration")
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
		
		shell('''#!/bin/bash
				 rm -rf .settings bin resources src target testng-suites .git
				 rm -f .classpath .project pom.xml README.md .gitignore
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
		
		shell('''#!/bin/bash
				 rm -rf .settings bin resources src target testng-suites .git
				 rm -f .classpath .project pom.xml README.md .gitignore
		''')
    }
	publishers{
    downstreamParameterized{
      trigger(md_FolderName + "/Create_Issue"){
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
      trigger(cd_FolderName + "/Create_Department"){
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
		
		shell('''#!/bin/bash
				 rm -rf .settings bin resources src target testng-suites .git
				 rm -f .classpath .project pom.xml README.md .gitignore
		''')
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

//Use Case 1

retrieveConfig_4.with{
    description("This retrieves the configuration file")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmSet1Config)
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
      trigger(cp_FolderName + "/Enable_Feature"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

enablefeature.with{
    description("This create a Departmant in Oracle HCM Application")
	parameters{
		stringParam("B","","Build Number")
		stringParam("PARENT_BUILD","","Parent Build Job")
		}
    wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmEnableFeatRepo)
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
		
		shell('''#!/bin/bash
				 rm -rf .settings bin resources src target testng-suites .git
				 rm -f .classpath .project pom.xml README.md .gitignore
		''')
    }
	publishers{
    downstreamParameterized{
      trigger(cp_FolderName + "/Create_Project"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }	
}

createproject.with{
    description("This create an Implementation project in in Oracle HCM Application")
	parameters{
		stringParam("B","","Build Number")
		stringParam("PARENT_BUILD","","Parent Build Job")
		}
    wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmapp)
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
		goals('clean install')
		mavenInstallation("ADOP Maven")
	}
		shell('''#!/bin/bash
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Create_Project/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Implementation Project" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Retrieve_Configuration/workspace
		cd ..
			mkdir screenshots 
			cd screenshots       
			cp -avr $WORKSPACE/target/screenshots/* .
			cd ..
			rm -rf $WORKSPACE/*
			rm -rf $WORKSPACE/.git $WORKSPACE/.settings
			rm -f $WORKSPACE/.classpath $WORKSPACE/.project
        mv screenshots $WORKSPACE		
		''')
	}
	publishers{
    downstreamParameterized{
      trigger(cp_FolderName + "/Create_User"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }	
}

createuser.with{
    description("This create an User Access in in Oracle HCM Application")
	parameters{
		stringParam("B","","Build Number")
		stringParam("PARENT_BUILD","","Parent Build Job")
		}
    wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmApp)
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
		goals('clean install')
		mavenInstallation("ADOP Maven")
	}
		shell('''#!/bin/bash
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Create_User/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Create Implementation User" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Retrieve_Configuration/workspace
		cd ..
			mkdir screenshots 
			cd screenshots       
			cp -avr $WORKSPACE/target/screenshots/* .
			cd ..
			rm -rf $WORKSPACE/*
			rm -rf $WORKSPACE/.git $WORKSPACE/.settings
			rm -f $WORKSPACE/.classpath $WORKSPACE/.project
        mv screenshots $WORKSPACE		
		''')
	}
	publishers{
    downstreamParameterized{
      trigger(cp_FolderName + "/Add_User_Data_Role"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }	
}

adddatarole.with {
  description("This job add data role to the user in Oracle HCM Application")
	parameters{
		stringParam("B","","Build Number")
		stringParam("PARENT_BUILD","","Parent Build Job")
		}
    wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmApp)
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
		goals('clean install')
		mavenInstallation("ADOP Maven")
	}
		shell('''#!/bin/bash
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Add_User_Data_Role/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Create Data Role for Implementation Users" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Retrieve_Configuration/workspace
		cd ..
			mkdir screenshots 
			cd screenshots       
			cp -avr $WORKSPACE/target/screenshots/* .
			cd ..
			rm -rf $WORKSPACE/*
			rm -rf $WORKSPACE/.git $WORKSPACE/.settings
			rm -f $WORKSPACE/.classpath $WORKSPACE/.project
        mv screenshots $WORKSPACE		
		''')
	}
	publishers{
    downstreamParameterized{
      trigger(cp_FolderName + "/Apply_Data_Role"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }	
}

applydatarole.with {
  description("This job apply the data role to the user in Oracle HCM Application")
	parameters{
		stringParam("B","","Build Number")
		stringParam("PARENT_BUILD","","Parent Build Job")
		}
    wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmApp)
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
		goals('clean install')
		mavenInstallation("ADOP Maven")
	}
		shell('''#!/bin/bash
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Apply_Data_Role/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Provision Role to Implementation Users" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Retrieve_Configuration/workspace
		cd ..
			mkdir screenshots 
			cd screenshots       
			cp -avr $WORKSPACE/target/screenshots/* .
			cd ..
			rm -rf $WORKSPACE/*
			rm -rf $WORKSPACE/.git $WORKSPACE/.settings
			rm -f $WORKSPACE/.classpath $WORKSPACE/.project
        mv screenshots $WORKSPACE		
		''')
	}
	publishers{
    downstreamParameterized{
      trigger(cp_FolderName + "/Manage_Currencies"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }	
}

managecurrencies.with{
    description("This job manage the Currencies in Oracle HCM Application")
	parameters{
		stringParam("B","","Build Number")
		stringParam("PARENT_BUILD","","Parent Build Job")
		}
    wrappers {
        preBuildCleanup()
        sshAgent("adop-jenkins-master")
    }
    scm{
        git{
            remote{
                url(hcmApp)
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
		goals('clean install')
		mavenInstallation("ADOP Maven")
	}
		shell('''#!/bin/bash
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Apply_Data_Role/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Currencies" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Retrieve_Configuration/workspace
		cd ..
			mkdir screenshots 
			cd screenshots       
			cp -avr $WORKSPACE/target/screenshots/* .
			cd ..
			rm -rf $WORKSPACE/*
			rm -rf $WORKSPACE/.git $WORKSPACE/.settings
			rm -f $WORKSPACE/.classpath $WORKSPACE/.project
        mv screenshots $WORKSPACE		
		''')
	}
}



