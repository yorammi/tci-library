package tci.multiJob;

class stepsBlock implements Serializable {

    def script
    def step1
    def step2
    def step3

    stepsBlock(script) {
        this.script = script

        script.echo "before echo 1"
        step1 = "echo test1"
        script.echo "before echo 2"
        step2 = "echo test2"
        script.echo "before echo 3"
        step3 = "echo test3"
    }

}

