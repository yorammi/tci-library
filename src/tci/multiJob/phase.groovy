package tci.multiJob;

import tci.multiJob.stepsBlock

class phase implements Serializable {

    def script
    def job1
    def job2

    phase(script) {
        this.script = script

        job1 = phaseJob.newInstance(script)
        job1.jobName = "test-tciEnv"
        job2 = phaseJob.newInstance(script)
        job2.jobName = "test-tciGit"

    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

            parallelBlocks[job1.jobName] = {
                script.build job: job1.jobName, wait: true
            }
            parallelBlocks[job2.jobName] = {
                script.build job: job2.jobName, wait: true
            }
            script.parallel parallelBlocks
        }
    }

}

