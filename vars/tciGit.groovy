#!/usr/bin/groovy

//def checkout(String repositoryUrl, String branch, String credentialsId, String relativPath=".") {
def checkout(Map config) {
    if (config == null) {
        config = [:]
    }
    if (config.url == null || config.url == "") {
        echo "[ERROR] repository URL must be provided!"
        currentBuild.result = "FAILURE"
    }
    if (config.branch == null) {
        config.branch = "master"
    }
    if (config.credentialsId == null) {
        if (env.TCI_MASTER_DEFAULT_GIT_CREDENTIAL) {
            config.credentialsId = env.TCI_MASTER_DEFAULT_GIT_CREDENTIAL
        }
        else {
            config.credentialsId = "gitsshkey"
        }
    }
    if (config.dir == null) {
        config.dir = "."
    }

    dir(config.dir) {
        timeout(time: 5, unit: 'MINUTES') {
            checkout([
                $class: 'GitSCM', branches: [[name: config.branch]],
                userRemoteConfigs: [[url: config.url ,credentialsId:config.credentialsId]]
            ])
        }
    }
}

