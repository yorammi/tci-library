package tci.multiJob;

class stepsBlock implements Serializable {

    def script
    def jobName

    stepsBlock(script) {
        this.script = script
    }

}

