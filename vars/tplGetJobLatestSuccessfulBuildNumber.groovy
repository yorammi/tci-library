def call(String jobName) {
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