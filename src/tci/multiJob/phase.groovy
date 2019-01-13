package tci.multiJob;

import tci.multiJob.phaseJob

class phase implements Serializable {

    def script
    def jobs = []
    def job1
    def job2

    phase(script) {
        this.script = script
    }

    void addJob(Map config) {
        if (config.jobName == null) {
            throw ("[ERROR] You must provide a jobName!")
        }
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]
            def counter=1
            script.echo jobs
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

