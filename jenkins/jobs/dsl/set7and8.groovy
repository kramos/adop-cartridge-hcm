// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def set7_FolderName = hfm_FolderName + "/Set_7_and_8"

// Repositories
def hcmSet7Config = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set_7_8_Config"
def hcmApp = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_App_Repo"

// Jobs
def retrieveConfig = freeStyleJob(set7_FolderName + "/Retrieve_Configuration")
def reportingestablishment = freeStyleJob(set7_FolderName + "/Reporting_Establishments")
def disablityorganization = freeStyleJob(set7_FolderName + "/Disablity_Organizations")
def organizationtrees = freeStyleJob(set7_FolderName + "/Organization_Trees")
def managegrade = freeStyleJob(set7_FolderName + "/Manage_Grade")
def graderates = freeStyleJob(set7_FolderName + "/Grade_Rates")
def gradeladders = freeStyleJob(set7_FolderName + "/Grade_Ladders")
def jobfamilies = freeStyleJob(set7_FolderName + "/Job_Families")
def managejob = freeStyleJob(set7_FolderName + "/Manage_Job")
def managepositions = freeStyleJob(set7_FolderName + "/Manage_Positions")

// Pipeline
def usecase7_pipeline = buildPipelineView(set7_FolderName + "/Set_7_and_8")

usecase7_pipeline.with{
    title('Set 7 and 8')
    displayedBuilds(5)
    selectedJob(set7_FolderName + "/Retrieve_Configuration")
    showPipelineParameters()
    refreshFrequency(5)
}


// Set 7 and 8 jobs

retrieveConfig.with{
    description("This retrieves the configuration file that will be used as a template for use case set 2 to the Oracle HCM Application")
    wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(hcmSet7Config)
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
      trigger(set7_FolderName + "/Business_Unit"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

reportingestablishment.with{
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
      trigger(set7_FolderName + "/Disablity_Organizations"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

disablityorganization.with{
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
      trigger(set7_FolderName + "/Organization_Trees"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

organizationtrees.with {
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
      trigger(set7_FolderName + "/Manage_Grade"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

managegrade.with{
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
		shell('''java -jar /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_3/jobs/Legal_Entity_Registration/workspace/target/HCM-0.0.1-SNAPSHOT.jar -r "Manage Legislative Data Groups" -w $WORKSPACE -e /var/jenkins_home/jobs/Oracle/jobs/HCM/jobs/HCM_Features_Manager/jobs/Set_3/jobs/Retrieve_Configuration/workspace
		in progress	
		''')
	}
	publishers{
    downstreamParameterized{
      trigger(set7_FolderName + "/Grade_Rates"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

graderates.with{
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
      trigger(set7_FolderName + "/Grade_Ladders"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

gradeladders.with{
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
      trigger(set7_FolderName + "/Job_Families"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

jobfamilies.with{
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
      trigger(set7_FolderName + "/Manage_Job"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

managejob.with{
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
      trigger(set7_FolderName + "/Manage_Positions"){
        condition("SUCCESS")
		  parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

managepositions.with{
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
}

