#!/usr/bin/groovy

def blockDirectManualJobTriggering()
{
    def upstream = currentBuild.rawBuild.getCause(hudson.model.Cause$UpstreamCause)
    if (upstream == null)
    {
        error("***** ERROR: YOU CANNOT ACTIVATE THIS JOB DIRECTLY!!! *****")
    }
}

def getJobLatestSuccessfulBuildNumber(String jobName) {
    try{
        def job = Jenkins.getInstance().getItemByFullName(jobName, Job.class)
        def build = job.getLastSuccessfulBuild()
        return build.getNumber()
    }
    catch (error)
    {
        return 0
    }
}

def keepBuildForever() {
    def job = Jenkins.getInstance().getItemByFullName(env.JOB_BASE_NAME, Job.class)
    def build = job.getBuildByNumber(env.BUILD_ID as int)
    build.keepLog(true)
}

def setJobEnabledStatus(String jobName, boolean jobEnabledStatus) {
    try
    {
        if(jobEnabledStatus)
        {
            Jenkins.instance.getItem(jobName).doEnable()
            echo "The job '${jobName}' enabled"
        }
        else
        {
            Jenkins.instance.getItem(jobName).doDisable()
            echo "The job '${jobName}' disabled"
        }
    }
    catch (error)
    {
        echo error.message
    }

}

def setJobNextBuildNumber(String jobName, Integer jobNextBuildNumber) {
    Jenkins.instance.getItemByFullName(jobName).updateNextBuildNumber(jobNextBuildNumber)
}

@NonCPS
def setRandomBuildStatus() {

    try
    {
        randomStatus = "SUCCESS"
        randomNumber = Math.abs(new Random().nextInt() % 4)
        switch (randomNumber)
        {
            case 0:
                randomStatus = "SUCCESS"
                break;
            case 1:
                randomStatus = "UNSTABLE"
                break;
            case 2:
                randomStatus = "FAILURE"
                break;
            case 3:
                randomStatus = "ABORTED"
                break;
            default:
                randomStatus = "SUCCESS"
                break;
        }
        echo "Setting job status (by random) to be: "+randomStatus
        currentBuild.result = randomStatus
    }
    catch(error)
    {
        echo error.message
    }
}
