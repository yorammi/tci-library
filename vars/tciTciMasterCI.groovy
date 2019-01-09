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
                        sh '''
                            tciGit.gitCheckout(repoUrl: "git@github.com:TikalCI/tci-bloody-jenkins.git", dir: 'tci-bloody-jenkins', branch: 'master')
                            tciGit.gitCheckout(repoUrl: "git@github.com:TikalCI/tci-master.git",dir: 'tci-master', branch: 'master')
                            cp tci-bloody-jenkins/plugins.txt plguins.txt
                            cat tci-master/src/resources/tci/plugins-${TYPE}.txt >> plugins.txt
                            cat plugins.txt | sort > tci-bloody-jenkins/plugins.txt
                        '''
                    }
                }
            }
            stage("Docker build") {
                steps {
                    script {
                        sh '''
                            docker build -t tikalci/tci-master-${TYPE}:latest tci-bloody-jenkins
                            docker tag tikalci/tci-master-${TYPE}:latest tikalci/tci-master-${TYPE}:${VERSIOPN}
                        '''
                    }
                }
            }
//            stage("Publish") {
//                steps {
//                    script {
//                        sh '''
//                            docker push tikalci/tci-master-${TYPE}:latest
//                            docker push tikalci/tci-master-${TYPE}:${VERSIOPN}
//                        '''
//                    }
//                }
//            }
        }
    }
}

