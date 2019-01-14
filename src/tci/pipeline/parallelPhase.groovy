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
    def jobs = []
    boolean failFast = false

    parallelPhase(script, boolean failFast = false) {
        this.script = script
        this.failFast = failFast
    }

    void addSubJob(Map config) {
        if (config == null) {
            config = [:]
        }
        if (config.job == null) {
            throw ("[ERROR] you must provive a job name to run!!!")
        }
        if (config.propagate == null) {
            config.propagate = false
        }
        if (config.wait == null) {
            config.wait = false
        }

        def job = new subJob(config.job, config.propagate, config.wait)
        jobs << job
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

            def counter=1
            jobs.each { item ->
                script.stage(item.jobName) {
                    parallelBlocks[item.jobName] = {
                        script.build (job: item.jobName, propagate: item.propagate , wait: item.wait)
                    }
                }
            }

//            script.tciGeneral.tciPhase {
                try {
                    parallelBlocks.failFast = failFast
                    script.parallel parallelBlocks
                }
                catch (error)
                {
                    throw ("[ERROR] TCI parallel phase failed!")
                }
            }
 //       }
    }

}

