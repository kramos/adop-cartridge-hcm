// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def hfm_FolderName = projectFolderName + "/HCM_Features_Manager"
def set2_FolderName = hfm_FolderName + "/Set_2"

// Repositories
def hcmSet2Config = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set2_Config"
def hcmConvRateTypesRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set2_ConversionRateTypes"
def hcmLegDataGrpRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set2_LegislativeDataGroups"
def hcmManageLegAddRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set2_ManageLegalAddress"
def hcmManageRefDataSetsRepo = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_Set2_ManageReferenceDataSets"


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
                url(hcmConvRateTypesRepo)
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
				 rm -rf .settings bin resources src target testng-suites
				 rm -f .classpath .project pom.xml README.md
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
                url(hcmManageLegAddRepo)
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
				 rm -rf .settings bin resources src target testng-suites
				 rm -f .classpath .project pom.xml README.md
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
    environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
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
                url(hcmManageRefDataSetsRepo)
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
				 rm -rf .settings bin resources src target testng-suites
				 rm -f .classpath .project pom.xml README.md
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
          goals('-P selenium-projectname-regression-test clean test')
          mavenInstallation("ADOP Maven")
        }
		
		shell('''#!/bin/bash
				 rm -rf .settings bin resources src target testng-suites
				 rm -f .classpath .project pom.xml README.md
		''')
    }
}



