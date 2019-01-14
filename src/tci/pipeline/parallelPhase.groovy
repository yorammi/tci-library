package tci.pipeline;

class parallelPhase implements Serializable {

    class subJob implements Serializable {

        String jobName
        boolean propagate
        boolean wait

        subJob(String jobName, boolean propagate, boolean wait ) {
            this.jobName = jobName
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
        if (config.wait == null) {
            config.wait = true
        }

        def job = new subJob(config.job, config.propagate, config.wait)
        jobs << job
    }

    void run() {
            def parallelBlocks = [:]

            def counter=1
            jobs.each { item ->
                parallelBlocks["Run job: "+item.jobName] = {
                    script.stage("Run job: "+item.jobName) {
                        script.tciLogger.info ("Starting job: ${item.jobName}")
                        script.build (job: item.jobName, propagate: item.propagate , wait: item.wait)
                        script.tciLogger.info ("Done running job: ${item.jobName}")
                    }
                }
            }

            script.tciGeneral.tciPhase (name) {
                try {
                    parallelBlocks.failFast = failFast
                    script.parallel parallelBlocks
                }
                catch (error)
                {
                    script.tciLogger.info ("[ERROR] TCI parallel phase failed!")
                    throw error
                }
        }
    }
}

