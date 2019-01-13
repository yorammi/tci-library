package tci.multiJob;

class stepsBlock implements Serializable {

    def steps = [:]

    stepsBlock(script) {
        this.script = script

        steps.values().add("echo test")
    }

}

