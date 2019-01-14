package tci.pipeline;

class parallelPhase implements Serializable {

    class parallelPhaseSubJob implements Serializable {

        def script
        def jobName
        boolean propagate

        parallelPhaseSubJob(script, String jobName,boolean propagate ) {
            this.script = script
            this.jobName = jobName
            this.propagate = propagate

        }
    }

    def script
    def jobs = []
    boolean failFast = false

    parallelPhase(script, boolean failFast = false) {
        this.script = script
        this.failFast = failFast
    }

    void addSubJob(String jobName, Map config) {
        if (config.propagate == null) {
            config.propagate = false
        }
        if (config.wait == null) {
            config.wait = false
        }

        def job = parallelPhaseSubJob.newInstance(script, jobName, config.propagate, config.wait)
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

            tciGeneral.tciPhase {
                try {
                    parallelBlocks.failFast = failFast
                    script.parallel parallelBlocks
                }
                catch (error)
                {
                    throw ("[ERROR] TCI parallel phase failed!")
                }
            }
        }
    }

}

