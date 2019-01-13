package tci.multiJob;

class stepsBlock implements Serializable {

    def steps = [:]

    @NonCPS
    stepsBlock(script) {
        this.script = script

        steps.values().add("echo test")
    }

}

