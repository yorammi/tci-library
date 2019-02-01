package tci.pipeline

import groovy.time.TimeCategory;

class parallelPhase implements Serializable {

    class subJob implements Serializable {

        String jobName
        def parameters
        boolean propagate
        boolean wait
        int retry

        subJob(String jobName, def parameters, boolean propagate, boolean wait, int retry ) {
            this.jobName = jobName
            this.parameters = parameters
            this.propagate = propagate
            this.wait = wait
            this.retry = retry
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
        if (config.retry == null) {
            config.retry = 1
        }

        def job = new subJob(config.job, config.parameters, config.propagate, config.wait, config.retry)
        jobs << job
    }

    void run() {
        def parallelBlocks = [:]

        def counter=1
        jobs.each { item ->
            def index = counter
//            parallelBlocks["Run job: "+item.jobName +" (${index})"] = {
//                script.stage("Run job: "+item.jobName) {
            parallelBlocks["Run job #"+counter+": "+item.jobName] = {
                script.stage("Run job #"+index+": "+item.jobName) {
                    def timeStart = new Date()
                    if( item.parameters != null) {
                        script.build (job: item.jobName, parameters: item.parameters, propagate: item.propagate , wait: item.wait, retry: item.retry)
                    }
                    if( item.retry > 1) {
                        script.retry (item.retry) {
                            script.build (job: item.jobName, parameters: item.parameters, propagate: item.propagate , wait: item.wait, retry: item.retry)
                        }
                    }
                    else {
                    }
                    def timeStop = new Date()
                    def duration = TimeCategory.minus(timeStop, timeStart)
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

