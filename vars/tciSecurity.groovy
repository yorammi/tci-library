#!/usr/bin/groovy

def configureAWS(String credentialsId) {
    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: credentialsId, accessKeyVariable: 'CRED_AWS_ACCESS_KEY_ID', secretKeyVariable: 'CRED_AWS_SECRET_ACCESS_KEY']]) {
        env.AWS_ACCESS_KEY_ID=CRED_AWS_ACCESS_KEY_ID
        env.AWS_SECRET_ACCESS_KEY=CRED_AWS_SECRET_ACCESS_KEY
        sh "mkdir -p ~/.aws"
        sh "echo '[default]\naws_access_key_id = ${env.CRED_AWS_ACCESS_KEY_ID}\naws_secret_access_key = ${env.CRED_AWS_SECRET_ACCESS_KEY}' > ~/.aws/credentials"
    }
}

def addNewSshKey(String credentialsId, String fileName='a.pem') {
    withCredentials([sshUserPrivateKey(credentialsId: credentialsId, keyFileVariable: 'SSH_KEY_FILE_PATH', passphraseVariable: 'SSH_PASSPHRASE', usernameVariable: 'SSH_USERNAME')]) {
        sh '''
            mkdir -p ~/.ssh
            cp ${SSH_KEY_FILE_PATH} ~/.ssh/${fileName}
            eval `ssh-agent -s`; ssh-add ~/.ssh/${fileName}
            echo 'Host * \n\tStrictHostKeyChecking no\n\tUser ubuntu\n\tIdentityFile ~/.ssh/${fileName}' >> ~/.ssh/config
        '''
    }
}

def setGoogleCloudCredentialJson(String credentialsId) {

    withCredentials([file(credentialsId: credentialsId, variable: 'CRED_JSON')])
            {
                def WORKSPACE_JSON=WORKSPACE+'/.Google_credentials.json'
                def input = readJSON file: CRED_JSON
                writeJSON file: WORKSPACE_JSON, json: input
                env.JSON=WORKSPACE_JSON
            }
}

