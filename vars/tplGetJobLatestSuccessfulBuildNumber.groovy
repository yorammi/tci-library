def call(String jobName) {
    tciGeneral.deprecatedMessage("tplGetJobLatestSuccessfulBuildNumber","tciJobs.getJobLatestSuccessfulBuildNumber")
    return tciGeneral.getJobLatestSuccessfulBuildNumber(jobName)
}