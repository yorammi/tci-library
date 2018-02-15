def call()
{
    def upstream = currentBuild.rawBuild.getCause(hudson.model.Cause$UpstreamCause)
    if (upstream == null)
    {
        error("***** ERROR: YOU CANNOT ACTIVATE THIS JOB DIRECTLY!!! *****")
    }
}