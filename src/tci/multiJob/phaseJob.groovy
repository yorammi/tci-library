package tci.multiJob;

class phaseJob implements Serializable {

    def script
    def jobName

    phaseJob(script) {
        this.script = script
    }

}

