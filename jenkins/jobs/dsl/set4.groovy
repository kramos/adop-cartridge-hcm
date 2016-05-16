// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def set4_FolderName = hfm_FolderName + "/Set_4"

// Repositories
def hcmSet4Config = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set4_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(set4_FolderName + "/Retrieve_Configuration")
def legalreportingunit = freeStyleJob(set4_FolderName + "/Legal_Reporting_Unit")
def legalreportingunitreg = freeStyleJob(set4_FolderName + "/Legal_Reporting_Unit_Registrations")
def legalreportingunithcminfo = freeStyleJob(set4_FolderName + "/Legal_Reporting_Unit_HCM_Information")

// Pipeline
def usecase4_pipeline = buildPipelineView(set4_FolderName + "/Set_4")

usecase4_pipeline.with{
    title('Set 4')
    displayedBuilds(5)
    selectedJob(set4_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}


// Set 4 jobs

retrieveConfig.with{
    description("This retrieves the configuration file that will be used as a template for use case set 2 to the Oracle HCM Application")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmSet4Config)
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
      trigger(set4_FolderName + "/Legal_Reporting_Unit"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

legalreportingunit.with{
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_4/jobs/Legal_Reporting_Unit/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Legal Reporting Unit" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_4/jobs/Retrieve_Configuration/workspace
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
      trigger(set4_FolderName + "/Legal_Reporting_Unit_Registrations"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

legalreportingunitreg.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_4/jobs/Legal_Reporting_Unit_Registrations/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Legal Reporting Unit Registrations" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_4/jobs/Retrieve_Configuration/workspace
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
      trigger(set4_FolderName + "/Legal_Reporting_Unit_HCM_Information"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

legalreportingunithcminfo.with {
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
				java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_4/jobs/Legal_Reporting_Unit_HCM_Information/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Legal Reporting Unit HCM Information" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_4/jobs/Retrieve_Configuration/workspace
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


