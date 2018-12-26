#!/usr/bin/groovy

def deprecatedMessage(String oldStepName, String newStepName) {
    echo "[WARNING] The step '"+oldStepName+"' is deprecated. please use '"+newStepName+"' instead."
}

@NonCPS
def timedEcho(String text) {

    def out
    def config = new HashMap()
    def bindings = getBinding()
    config.putAll(bindings.getVariables())
    out = config['out']

    def now = new Date()
    def tstamp = now.format("yyyyMMdd-HH:mm:ss.SSS", TimeZone.getTimeZone('UTC'))

    echo "["+tstamp+"] " + text;
}

def setStatusByLogText(String searchText) {

    echo "Activating tciGeneral.setStatusByLogText";
    String logText = currentBuild.rawBuild.getLog()
    if(logText.contains(searchText))
    {
        println("Found '${searchText}' in build log")
        currentBuild.result = 'FAILURE'
    }
}

def findStringInBuildLog(String findText) {

    String logText = currentBuild.rawBuild.getLog()

    if(logText.contains(findText))
    {
        println("Found ${findText} in build log")
        return true
    }
    return false
}

def clearWorkspaceAsRoot() {

    try
    {
        docker.image("library/ubuntu").inside('-u root')
                {
                    sh ("find ${WORKSPACE} -mindepth 1 -delete > /dev/null | true")
                    sh ("chmod -R 777 ${WORKSPACE} > /dev/null | true")
                }
    }
    catch(error)
    {

    }
}
