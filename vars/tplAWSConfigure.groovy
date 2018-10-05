def call(String credentialsId) {

    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: credentialsId, accessKeyVariable: 'CRED_AWS_ACCESS_KEY_ID', secretKeyVariable: 'CRED_AWS_SECRET_ACCESS_KEY']])
    {
        env.AWS_ACCESS_KEY_ID=CRED_AWS_ACCESS_KEY_ID
        env.AWS_SECRET_ACCESS_KEY=CRED_AWS_SECRET_ACCESS_KEY
        sh "mkdir -p ~/.aws"
        sh "echo '[default]\naws_access_key_id = ${env.CRED_AWS_ACCESS_KEY_ID}\naws_secret_access_key = ${env.CRED_AWS_SECRET_ACCESS_KEY}' > ~/.aws/credentials"
    }
}