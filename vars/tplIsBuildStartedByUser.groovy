def call() {
    try{
        def isStartedByUser = currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause) != null
        return isStartedByUser
    }
    catch (Exception error)
    {
        return false
    }
}