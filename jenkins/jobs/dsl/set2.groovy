// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def set2_FolderName = hfm_FolderName + "/Set_2"

// Repositories
def hcmSet2Config = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set2_Config"
def hcmEstablishEnterpriseStrucRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set2_EstablishenterpriseStructure"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"


// Jobs
def retrieveConfig = freeStyleJob(set2_FolderName + "/Retrieve_Configuration")
def ratetypes = freeStyleJob(set2_FolderName + "/Conversion_Rate_Types")
def legdatagroups = freeStyleJob(set2_FolderName + "/Legislative_Data_Groups")
def legaladdress = freeStyleJob(set2_FolderName + "/Manage_Legal_Address")
def datasets = freeStyleJob(set2_FolderName + "/Manage_Reference_Data_Sets")
def hcmEstablishEntStrcut = freeStyleJob(set2_FolderName + "/Establish_Enterprise_Structure")

// Pipeline
def usecase2_pipeline = buildPipelineView(set2_FolderName + "/Set2")

usecase2_pipeline.with{
    title('Set 2')
    displayedBuilds(5)
    selectedJob(set2_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}


// Set 2 jobs

retrieveConfig.with{
    description("This retrieves the configuration file that will be used as a template for use case set 2 to the Oracle HCM Application")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmSet2Config)
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
      trigger(set2_FolderName + "/Conversion_Rate_Types"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

ratetypes.with{
    description("This Converts Rate Types in HCM Application")
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_2/jobs/Conversion_Rate_Types/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Conversion Rate Types" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_2/jobs/Retrieve_Configuration/workspace
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
      trigger(set2_FolderName + "/Manage_Legal_Address"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

legaladdress.with{
    description("This job Manage Legal Address in HCM Application")
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_2/jobs/Manage_Legal_Address/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Legal Addresses" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_2/jobs/Retrieve_Configuration/workspace
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
      trigger(set2_FolderName + "/Establish_Enterprise_Structure"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

hcmEstablishEntStrcut.with {
	description("Establish Enterprise Structure in HCM Application")
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
                url(hcmEstablishEnterpriseStrucRepo)
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
      trigger(set2_FolderName + "/Manage_Reference_Data_Sets"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

datasets.with{
    description("This job Manage Reference Data Sets in HCM Application")
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_2/jobs/Manage_Reference_Data_Sets/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Reference Data Sets" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_2/jobs/Retrieve_Configuration/workspace
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
      trigger(set2_FolderName + "/Legislative_Data_Groups"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

legdatagroups.with{
    description("Legislative Data Groups in HCM Application")
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
                url(hcmLegDataGrpRepo)
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
		java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_2/jobs/Legislative_Data_Groups/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Legislative Data Groups" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_2/jobs/Retrieve_Configuration/workspace
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



