// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def set13_FolderName = hfm_FolderName + "/Set_13"

// Repositories
def hcmSet13Config = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set13_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(set13_FolderName + "/Retrieve_Configuration")
def consolidationgrp = freeStyleJob(set13_FolderName + "/Manage_Consolidation_Groups")
def fastformulas = freeStyleJob(set13_FolderName + "/Manage_Fast_Formulas")
def elements = freeStyleJob(set13_FolderName + "/Manage_Elements")
def roleprovisioning = freeStyleJob(set13_FolderName + "/Manage_Role_Provisioning_Rules")
def commonlookups = freeStyleJob(set13_FolderName + "/Manage_Common_Lookups")

// Pipeline
def usecase13_pipeline = buildPipelineView(set13_FolderName + "/Set_13")

usecase13_pipeline.with{
    title('Set 13')
    displayedBuilds(5)
    selectedJob(set13_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}

// Set 13

retrieveConfig.with{
    description("This retrieves the configuration file that will be used as a template for use case set 2 to the Oracle HCM Application")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmSet13Config)
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
      trigger(set13_FolderName + "/Manage_Consolidation_Groups"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

consolidationgrp.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Manage_Consolidation_Groups/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Consolidation Groups" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Retrieve_Configuration/workspace
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
      trigger(set13_FolderName + "/Manage_Fast_Formulas"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

fastformulas.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Manage_Fast_Formulas/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Fast Formulas" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Retrieve_Configuration/workspace
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
      trigger(set13_FolderName + "/Manage_Elements"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

elements.with {
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Manage_Elements/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Elements" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Retrieve_Configuration/workspace
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
      trigger(set13_FolderName + "/Manage_Role_Provisioning_Rules"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

roleprovisioning.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Manage_Role_Provisioning_Rules/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Role Provisioning Rules" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Retrieve_Configuration/workspace
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
      trigger(set13_FolderName + "/Manage_Common_Lookups"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

commonlookups.with{
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
java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Manage_Common_Lookups/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Common Lookups" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_13/jobs/Retrieve_Configuration/workspace
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