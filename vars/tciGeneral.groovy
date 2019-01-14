#!/usr/bin/groovy

import hudson.tasks.test.AbstractTestResultAction
import groovy.time.*

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

def getTestsSummary() {

    def testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    def summary = ""

    if (testResultAction != null) {
        def total = testResultAction.getTotalCount()
        def failed = testResultAction.getFailCount()
        def skipped = testResultAction.getSkipCount()

        summary = "[Tests] "
        summary = summary + ("Total: " + total)
        summary = summary + (", Passed: " + (total - failed - skipped))
        summary = summary + (", Failed: " + failed)
        summary = summary + (", Skipped: " + skipped)
        env.TESTS_SUMMARY="${summary}"
        if(failed!=0)
        {
            currentBuild.result = "UNSTABLE"
        }
    }
    else
    {
        summary = "[Tests] No tests found"
    }
    return summary
}

def filterBuildLogErrors() {

    try
    {
        ArrayList<String> logText = currentBuild.rawBuild.getLog(200)

        String output = ""
        Integer count = 0
        for (String line : logText) {
            count++
            if( line.toLowerCase().contains('error') | line.toLowerCase().contains('exception'))
            {
                if("${output}" == "")
                {
                    output="[Line #${count}] "+line
                }
                else
                {
                    output+="\n[Line #${count}] "+line
                }
            }
        }
        return output
    }
    catch(error)
    {
        echo error.message
        return ""
    }
}

def saveBuildLogFile() {

    try
    {
        logName=JOB_NAME+'_'+BUILD_NUMBER+'_JenkinsBuild.log'
        ArrayList<String> logText = currentBuild.rawBuild.getLog(20000)

        def logFile = new File(WORKSPACE+'/'+logName)
        logFile.text=''
        for (String line : logText)
        {
            logFile.text+= line+"\n"
        }

        archive "**/"+logName
    }
    catch(error)
    {}
}

def tciPhase(String phaseName, Closure body) {
    tciLogger.info(phaseName+ " phase started")
    def timeStart = new Date()
    body()
    def timeStop = new Date()
    def duration = TimeCategory.minus(timeStop, timeStart)
    tciLogger.info(phaseName+ " phase ended.")
    tciLogger.info(phaseName+ " phase duration: ${duration}")
}





