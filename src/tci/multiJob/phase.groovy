package tci.multiJob;

import tci.multiJob.phaseJob

class phase implements Serializable {

    def script
    def jobs = []

    phase(script) {
        this.script = script
    }

    void addJob(Map config) {
        echo config.jobName
        if (config.jobName == null) {
            throw ("[ERROR] You must provide a jobName!")
        }
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]
            def counter=1
            jobs.each { item ->
                script.echo item.jobName
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

