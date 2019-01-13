package tci.multiJob;

import tci.multiJob.phaseJob

class phase implements Serializable {

    def script
    def jobs = []
    boolean failFast = false

    phase(script, boolean failFast = false) {
        this.script = script
        this.failFast = failFast
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

            def counter = 0
            while (counter < test.parallelBlocks()) {
                parallelBlocks[parallelBlocks[counter]+':'+counter] = {
                    script.stage(parallelBlocks[counter]+':'+counter) {
                        script.build (job: parallelBlocks[counter], wait: true)
                    }
                }
                counter++
            }

//            def counter=1
//            jobs.each { item ->
//                def currentIndex = counter
//                parallelBlocks[item.jobName+':'+currentIndex] = {
//                    script.stage(item.jobName+':'+currentIndex) {
//                        script.build (job: item.jobName, wait: true)
//                    }
//                }
//                counter++
//            }

            parallelBlocks.failFast = failFast
            script.parallel parallelBlocks
        }
    }

}

