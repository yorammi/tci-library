#!/usr/bin/groovy

@NonCPS
def installPythonModules(String requirementsFilePath) {
    File file = new File( env.WORKSPACE + "/" + requirementsFilePath )
    if( file.exists() && file.isFile()) {
        sh "pip install -r ${pythonRequirements}"
    }
}

@NonCPS
def runPythonScript(String scriptFilePath) {
    File file = new File( env.WORKSPACE + "/" + scriptFilePath )
    if( file.exists() && file.isFile()) {
        sh "python ./${scriptFilePath}"
    }
}

