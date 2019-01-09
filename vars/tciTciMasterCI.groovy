#!/usr/bin/groovy

def call() {
    pipeline {
        agent { label 'tci-jnlp-node' }
        options {
            timestamps()
            buildDiscarder(logRotator(numToKeepStr: '30'))
            ansiColor('xterm')
        }
        parameters {
            choice (
                    choices: 'minimal\nfull',
                    description: 'Which tci-master version to build: minimal or full',
                    name : 'TYPE')
            string (
                    defaultValue: '',
                    description: 'tci-master version',
                    name : 'VERSION')
        }

        stages {
            stage("Setup") {
                steps {
                    script {
                        currentBuild.displayName += " [TYPE] ${params.TYPE} [VERSION] ${params.VERSION}"
                    }
                }
            }
        }
    }
}

