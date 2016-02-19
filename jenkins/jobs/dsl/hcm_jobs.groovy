// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def configGitRepo = "HCM_Configurations"
def wacGitRepo = "OracleHCM_Java"
def validationGitRepo = "OracleHCM_Validation"
def preConfigGitRepo2 = "Predefined_configuration(2)"
def preConfigGitRepo3 = "Predefined_configurations(3)"

def configGitRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/" + configGitRepo
def wacGitRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/" + wacGitRepo
def validationGitRepoUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/" + validationGitRepo
def preConfigGitRepo2Url = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/" + preConfigGitRepo2
def preConfigGitRepo3Url = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/" + preConfigGitRepo3

// Jobs
def buildAppJob = mavenJob(projectFolderName + "/1_Build")
def buildValJob = mavenJob(projectFolderName + "/2_BuildValidation")
def deployJob = freeStyleJob(projectFolderName + "/3_Deploy")
def validationJob = freeStyleJob(projectFolderName + "/4_Validation")
def createIssueJob = freeStyleJob(projectFolderName + "/CreateIssue")
def createIssueJob = freeStyleJob(projectFolderName + "/CreateProject")

// Views
def pipelineView = buildPipelineView(projectFolderName + "/HCM_Automation")

pipelineView.with{
    title('HCM Automation')
    displayedBuilds(5)
    selectedJob(projectFolderName + "/HCM_Automation")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

buildAppJob.with{
  description("This job builds Web Application Controller - WAC")
  wrappers {
    preBuildCleanup()
    injectPasswords()
    maskPasswords()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(wacGitRepoUrl)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  label("java8")
  triggers{
    gerrit{
      events{
        refUpdated()
      }
      configure { gerritxml ->
        gerritxml / 'gerritProjects' {
          'com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.data.GerritProject' {
            compareType("PLAIN")
            pattern(projectFolderName + "/" + wacGitRepo)
            'branches' {
              'com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.data.Branch' {
                compareType("PLAIN")
                pattern("master")
              }
            }
          }
        }
        gerritxml / serverName("ADOP Gerrit")
      }
    }
  }
  steps {
    maven{
      mavenInstallation("ADOP Maven")
    }
  }
  publishers{
    archiveArtifacts("**/*")
    downstreamParameterized{
      trigger(projectFolderName + "/Reference_Application_Unit_Tests"){
        condition("UNSTABLE_OR_BETTER")
        parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}


