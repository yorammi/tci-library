def call(String jobName, boolean jobEnabledStatus) {
    tciGeneral.deprecatedMessage("tplSetRandomBuildStatus","tciJobs.setRandomBuildStatus")
    tciGeneral.setRandomBuildStatus(jobName,jobEnabledStatus)
}
