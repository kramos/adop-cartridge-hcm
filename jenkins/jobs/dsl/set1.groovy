// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
//def md_FolderName = hfm_FolderName + "/Manage_Department"
//def cd_FolderName = hfm_FolderName + "/Create_Department"
//def pm_FolderName = hfm_FolderName + "/Person_Management"
def cp_FolderName = hfm_FolderName + "/Set_1"

// Repositories
//def hcmManDepConRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDConfiguration"
//def hcmManDepAppRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDApplication"
//def hcmEmpManAppRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_MDPersonManagement"
def hcmSet1Config = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set1_Config"
def hcmEnableFeatRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set1_EnableFeature"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(cp_FolderName + "/Retrieve_Configuration")
def enablefeature = freeStyleJob(cp_FolderName + "/Enable_Feature")
def createproject = freeStyleJob(cp_FolderName + "/Create_Project")
def managecurrencies = freeStyleJob(cp_FolderName + "/Manage_Currencies")
def createuser = freeStyleJob(cp_FolderName + "/Create_User")
def adddatarole = freeStyleJob(cp_FolderName + "/Add_User_Data_Role")
def applydatarole = freeStyleJob(cp_FolderName + "/Apply_Data_Role")

// Pipeline
def usecase1_pipeline = buildPipelineView(cp_FolderName + "/Set1")

usecase1_pipeline.with{
    title('Set 1')
    displayedBuilds(5)
    selectedJob(cp_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

//Use Case 1

retrieveConfig.with{
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Create_User/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Create Implementation Users" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Retrieve_Configuration/workspace
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Add_User_Data_Role/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Create Data Roles for Implementation Users" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Retrieve_Configuration/workspace
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Manage_Currencies/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Currencies" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_1/jobs/Retrieve_Configuration/workspace
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



