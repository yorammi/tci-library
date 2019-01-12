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

def configureAWS(String credentialsId) {
    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: credentialsId, accessKeyVariable: 'CRED_AWS_ACCESS_KEY_ID', secretKeyVariable: 'CRED_AWS_SECRET_ACCESS_KEY']]) {
        env.AWS_ACCESS_KEY_ID=CRED_AWS_ACCESS_KEY_ID
        env.AWS_SECRET_ACCESS_KEY=CRED_AWS_SECRET_ACCESS_KEY
        sh "mkdir -p ~/.aws"
        sh "echo '[default]\naws_access_key_id = ${env.CRED_AWS_ACCESS_KEY_ID}\naws_secret_access_key = ${env.CRED_AWS_SECRET_ACCESS_KEY}' > ~/.aws/credentials"
    }
}
