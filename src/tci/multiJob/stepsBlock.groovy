package tci.multiJob;

class stepsBlock implements Serializable {

    def script
    def steps = new String[3]

    stepsBlock(script) {
        this.script = script

        steps[0] = "echo test"
    }

}

