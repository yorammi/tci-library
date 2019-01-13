package tci.multiJob;

class stepsBlock implements Serializable {

    def script
    String[] steps = [:]

    stepsBlock(script) {
        this.script = script

        script.echo "before echo 1"
        steps[0] = "echo test1"
        script.echo "before echo 2"
        steps[1] = "echo test2"
        script.echo "before echo 3"
        steps[2] = "echo test3"
    }

}

