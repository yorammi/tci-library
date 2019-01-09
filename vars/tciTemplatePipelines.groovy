@Library ("tci-library") _

def oneStepPipeline() {
    pipeline {
        agent { label 'tci-jnlp-node' }
        options {
            timestamps()
            buildDiscarder(logRotator(numToKeepStr: '30'))
            ansiColor('xterm')
        }
        stages {
            stage("Step") {
                steps {
                    script {
                        def buildUserDisplayName = tciBuildUser.getBuildUserDisplayName()
                        currentBuild.displayName += " - activated by: ${buildUserDisplayName}"
                    }
                }
            }
        }
    }
}

