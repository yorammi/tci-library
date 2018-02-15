def call(String jobName, Integer jobNextBuildNumber) {
    Jenkins.instance.getItemByFullName(jobName).updateNextBuildNumber(jobNextBuildNumber)
}
