#!/usr/bin/groovy

def initEnv(Map config=null) {
    if (config == null) {
        config = [:]
    }
    if (config.deleteWorkspace == null) {
        config.deleteWorkspace = true
    }

    if(config.deleteWorkspace)
    {
        deleteWorkspace()
    }
    tciBuildUser.initBuildUserEnvVars()
}

def deleteWorkspace() {
    step([$class: 'WsCleanup'])
}

def deleteWorkspaceAsRoot() {

    try {
        docker.image("library/ubuntu").inside('-u root') {
            sh ("find ${WORKSPACE} -mindepth 1 -delete > /dev/null | true")
            sh ("chmod -R 777 ${WORKSPACE} > /dev/null | true")
        }
    }
    catch(error) {

    }
}

