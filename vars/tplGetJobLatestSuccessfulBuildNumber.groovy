def call(String jobName) {
    tciGeneral.deprecatedMessage("tplGetJobLatestSuccessfulBuildNumber","tciJobs.getJobLatestSuccessfulBuildNumber")
    tciGeneral.getJobLatestSuccessfulBuildNumber(jobName)
}