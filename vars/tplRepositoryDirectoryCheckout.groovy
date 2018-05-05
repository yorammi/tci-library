def call(String repositoryUrl, String branch, String credentialsId, String relativPath=".") {
    dir(relativPath)
    {
        timeout(time: 5, unit: 'MINUTES')
        {
            checkout([
                    $class: 'GitSCM', branches: [[name: branch]],
                    userRemoteConfigs: [[url: repositoryUrl ,credentialsId:credentialsId]]
            ])
        }
    }
}