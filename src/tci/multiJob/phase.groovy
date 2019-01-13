package tci.multiJob;

import tci.multiJob.phaseJob

class phase implements Serializable {

    def script
    def jobs = []
    boolean failFast = false

    phase(script) {
        this.script = script
    }

    void addJob(Map config) {
        if (config.jobName == null) {
            throw ("[ERROR] You must provide a jobName!")
        }

        def job = phaseJob.newInstance(script,config.jobName)
        jobs << job
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]
            def counter=1

            jobs.each { item ->
                def currentIndex = counter
                parallelBlocks[item.jobName+':'+currentIndex] = {
                    script.stage(item.jobName+':'+currentIndex) {
                        script.build (job: item.jobName, wait: true)
                    }
                }
                counter++
            }
            script.parallel parallelBlocks
        }
    }

}

