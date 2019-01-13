package tci.multiJob;

class stepsBlock implements Serializable {

    def script
    String[] steps = [:]

    stepsBlock(script) {
        this.script = script

        steps[0] = "echo test"
    }

}

