#!/usr/bin/groovy

def checkout(Map config) {
    try {
        if (!config) {
            echo "[DEBUG] config.url=${config.url}"
            config = [:]
            echo "[DEBUG] config.url=${config.url}"
        }
        if (!config.url || config.url == "") {
            echo "[ERROR] repository URL must be provided!"
            currentBuild.result = "FAILURE"
            return
        }
        if (!config.branch) {
            config.branch = "master"
        }
        if (!config.credentialsId) {
            if (env.TCI_MASTER_DEFAULT_GIT_CREDENTIAL) {
                config.credentialsId = env.TCI_MASTER_DEFAULT_GIT_CREDENTIAL
            }
            else {
                config.credentialsId = "gitsshkey"
            }
        }
        if (!config.dir) {
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
    catch (error) {
        echo "[ERROR] ${error}"
    }
}

