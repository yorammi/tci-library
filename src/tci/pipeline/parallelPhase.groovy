package tci.pipeline

import groovy.time.TimeCategory;

class parallelPhase implements Serializable {

    class subJob implements Serializable {

        String jobName
        def parameters
        boolean propagate
        boolean wait

        subJob(String jobName, def parameters, boolean propagate, boolean wait ) {
            this.jobName = jobName
            this.parameters = parameters
            this.propagate = propagate
            this.wait = wait
        }
    }

    def script
    def name
    def jobs = []
    boolean failFast = false

    parallelPhase(script, String name = "TCI parallel", boolean failFast = false) {
        this.script = script
        this.name = name
        this.failFast = failFast
    }

    void addSubJob(Map config) {
        if (config == null) {
            config = [:]
        }
        if (config.job == null) {
            script.tciLogger.info ("[ERROR] you must provive a job name to run!!!")
            throw Exception
        }
        if (config.propagate == null) {
            config.propagate = true
        }
        if (config.parameters == null) {
            config.parameters = null
        }
        if (config.wait == null) {
            config.wait = true
        }

        def job = new subJob(config.job, config.parameters, config.propagate, config.wait)
        jobs << job
    }

    void run() {
        def parallelBlocks = [:]

        def counter=1
        jobs.each { item ->
            def index = counter
            parallelBlocks["Run job: "+item.jobName +" (${index})"] = {
                script.stage("Run job: "+item.jobName) {
                    def timeStart = new Date()
                    script.tciLogger.info ("Starting job: ${item.jobName}")
                    if( item.parameters == null) {
                        script.build (job: item.jobName, propagate: item.propagate , wait: item.wait)
                    }
                    else {
                        script.build (job: item.jobName, parameters: item.parameters, propagate: item.propagate , wait: item.wait)
                    }
                    def timeStop = new Date()
                    def duration = TimeCategory.minus(timeStop, timeStart)
                    script.tciLogger.info ("Done running job: ${item.jobName}. Job duration:"+duration)
                }
            }
            counter++
        }

        script.tciPipeline.block (name:name) {
            parallelBlocks.failFast = failFast
            script.parallel parallelBlocks
        }
    }
}

