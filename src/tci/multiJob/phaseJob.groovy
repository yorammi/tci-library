package tci.multiJob;

class phaseJob implements Serializable {

    def script
    def jobName

    phaseJob(script,jobName) {
        this.script = script
        this.jobName = jobName
    }

}

