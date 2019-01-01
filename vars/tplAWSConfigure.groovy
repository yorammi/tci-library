def call(String credentialsId) {
    tciGeneral.deprecatedMessage("tplAWSConfigure","tciEnv.configureAWS")
    tciEnv.configureAWS(credentialsId)
}