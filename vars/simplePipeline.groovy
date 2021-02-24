def call(Map config) {
    if (config == null) {
        config = [:]
    }
    if (config.sleep == null)
    {
        config.sleep = 0
    }
    if (config.quietPeriod == null)
    {
        config.quietPeriod = 5
    }
    if (config.numToKeepStr == null)
    {
        config.numToKeepStr = "5"
    }

    pipeline {
        agent { label 'RCCBaseAgent' }
        options {
            timestamps()
            buildDiscarder(logRotator(numToKeepStr: config.numToKeepStr))
            ansiColor('xterm')
            skipDefaultCheckout()
            disableConcurrentBuilds()
            quietPeriod(config.quietPeriod)
        }
        parameters {
            string( name: 'STRING', defaultValue: '', description: 'simple string')
            booleanParam ( name: 'BOOLEAN', defaultValue: false, description: 'simple boolean')
        }
        stages {
            stage ("[Pipeline setup]") {
                steps {
                    script {
                        currentBuild.displayName "[STRING] ${STRING} [BOOLEAN] ${BOOLEAN}"
                    }
                }
            }
        }
        post {
            always {
                script {
                    sleep (config.sleep)
                }
            }
        }
    }
}