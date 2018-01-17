import hudson.tasks.test.AbstractTestResultAction

def call() {

    def testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    def summary = ""

    if (testResultAction != null) {
        def total = testResultAction.getTotalCount()
        def failed = testResultAction.getFailCount()
        def skipped = testResultAction.getSkipCount()

        summary = "[Tests] "
        summary = summary + ("Total: " + total)
        summary = summary + (", Passed: " + (total - failed - skipped))
        summary = summary + (", Failed: " + failed)
        summary = summary + (", Skipped: " + skipped)
        env.TESTS_SUMMARY="${summary}"
        if(failed!=0)
        {
            currentBuild.result = "UNSTABLE"
        }
    }
    else
    {
        summary = "[Tests] No tests found"
    }
    return summary
}

