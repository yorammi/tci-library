package tci.multiJob;

import tci.multiJob.phaseJob

class phase implements Serializable {

    def script
    def jobs = []
    def job1
    def job2

    phase(script) {
        this.script = script

        job1 = phaseJob.newInstance(script,"test-tciEnv")
        jobs << job1
        job2 = phaseJob.newInstance(script,"test-tciGit")
        jobs << job2

    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

            jobs.each{ parallelBlocks[it.jobName] = {
                stage(it.jobName) {
                    build job: it.jobName, wait: true
                }
            }
            }
//            parallelBlocks[job1.jobName] = {
//                script.build job: job1.jobName, wait: true
//            }
//            parallelBlocks[job2.jobName] = {
//                script.build job: job2.jobName, wait: true
//            }
            script.parallel parallelBlocks
        }
    }

}

