def call() {
    def job = Jenkins.getInstance().getItemByFullName(env.JOB_BASE_NAME, Job.class)
    def build = job.getBuildByNumber(env.BUILD_ID as int)
    def userId = build.getCause(Cause.UserIdCause).getUserId()
    return userId
}