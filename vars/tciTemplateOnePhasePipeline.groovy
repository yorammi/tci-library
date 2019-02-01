#!/usr/bin/groovy

def call() {
    pipeline {
        agent { label 'tci-jnlp-node' }
        options {
            timestamps()
            buildDiscarder(logRotator(numToKeepStr: '30'))
            ansiColor('xterm')
            skipDefaultCheckout()
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

