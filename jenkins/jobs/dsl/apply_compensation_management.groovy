//Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

//Repositories
def hcmApplyFeature = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/HCM_ApplyFeature"

//Jobs
def applyFeature = freeStyleJob(projectFolderName + "/Apply_Compensation_Management")

//Configure Jobs
applyFeature.with{
	description("This job creates an HCM implementation project with default compensation management feature.")
	wrappers {
		preBuildCleanup()
		sshAgent("adop-jenkins-master")
	}
	scm{
		git{
		  remote{
			url(hcmApplyFeature)
			credentials("adop-jenkins-master")
		  }
		  branch("*/master")
		}
	}
	steps {
		maven{
		  rootPOM('pom.xml')
		  goals('-P selenium-projectname-regression-test clean test')
		  mavenInstallation("ADOP Maven")
		}
	}
	environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
    }
	
}