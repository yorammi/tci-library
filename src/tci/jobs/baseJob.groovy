package tci.jobs;

abstract class baseJob implements Serializable {

    def logger
    def script

    baseJob(script) {
        this.script = script
    }
    
    void run() {
        script.timestamps() {
            try {
                script.tciLogger.info "Running with params: ${this.properties}"
                runImpl()
            } finally {
                try {
                } catch (all) {
                }

            }
        }
    }

    void runImpl() {
    }

    void runStage(String name, Closure stage) {
        script.tciLogger.info "--- Start stage [$name] ---"
        script.stage(name, stage)
        script.tciLogger.info "--- End stage [$name] ---"
    }
}
