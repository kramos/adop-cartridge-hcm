// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def set5_FolderName = hfm_FolderName + "/Set_5_and_6"

// Repositories
def hcmSet5Config = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set_5_6_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(set5_FolderName + "/Retrieve_Configuration")
def businessunit = freeStyleJob(set5_FolderName + "/Business_Unit")
def businessunitsetassignment = freeStyleJob(set5_FolderName + "/Business_Unit_Set_Assignment")
def valuesetforglobalhumanresources = freeStyleJob(set5_FolderName + "/Value_Sets_for_Global_Human_Resources")
def organizationstructuredescriptiveflexfields = freeStyleJob(set5_FolderName + "/Organization_Structure_Descriptive_Flexfields")
def enterprisehcminfo = freeStyleJob(set5_FolderName + "/Enterprise_HCM_Information")
def manageactions = freeStyleJob(set5_FolderName + "/Manage_Actions")
def actionreasons = freeStyleJob(set5_FolderName + "/Action_Reasons")
def managelocations = freeStyleJob(set5_FolderName + "/Manage_Locations")
def managedivsions = freeStyleJob(set5_FolderName + "/Manage_Divisions")
def managedept = freeStyleJob(set5_FolderName + "/Manage_Departments")
def depttrees = freeStyleJob(set5_FolderName + "/Manage_Department_Trees")

// Pipeline
def usecase5_pipeline = buildPipelineView(set5_FolderName + "/Set_5_and_6")

usecase5_pipeline.with{
    title('Set 5 and 6')
    displayedBuilds(5)
    selectedJob(set5_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}


// Set 5 and 6 jobs

retrieveConfig.with{
    description("This retrieves the configuration file that will be used as a template for use case set 2 to the Oracle HCM Application")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmSet5Config)
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
      trigger(set5_FolderName + "/Business_Unit"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

businessunit.with{
	parameters {
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Business_Unit/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Business Units" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Business_Unit_Set_Assignment"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

businessunitsetassignment.with{
	parameters {
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Business_Unit_Set_Assignment/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Business Unit Set Assignment" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Value_Sets_for_Global_Human_Resources"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

valuesetforglobalhumanresources.with {
	parameters {
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Value_Sets_for_Global_Human_Resources/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Value Sets" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Organization_Structure_Descriptive_Flexfields"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

organizationstructuredescriptiveflexfields.with{
	parameters {
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Organization_Structure_Descriptive_Flexfields/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Organization Structure Descriptive Flexfields" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Enterprise_HCM_Information"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

enterprisehcminfo.with{
	parameters {
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
		in progress		
		''')
	}
	publishers{
    downstreamParameterized{
      trigger(set5_FolderName + "/Manage_Actions"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

manageactions.with{
	parameters {
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Manage_Actions/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Actions" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Action_Reasons"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

actionreasons.with{
	parameters {
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Action_Reasons/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Action Reasons" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Manage_Locations"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

managelocations.with{
	parameters {
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
				java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Manage_Locations/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Locations" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Manage_Divisions"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

managedivsions.with{
	parameters {
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
				java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Manage_Divisions/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Divisions" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Manage_Departments"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

managedept.with{
	parameters {
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
				java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Manage_Departments/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Departments" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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
      trigger(set5_FolderName + "/Manage_Department_Trees"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

depttrees.with{
	parameters {
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Manage_Department_Trees/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Department Trees" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_5_and_6/jobs/Retrieve_Configuration/workspace
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



