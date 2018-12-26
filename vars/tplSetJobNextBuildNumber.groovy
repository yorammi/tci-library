def call(String jobName, Integer jobNextBuildNumber) {
    tciGeneral.deprecatedMessage("tplSetJobNextBuildNumber","tciJobs.setJobNextBuildNumber")
    tciGeneral.setJobNextBuildNumber(jobName,jobNextBuildNumber)
}
