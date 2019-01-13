package tci.multiJob;

import tci.multiJob.stepsBlock

class phase implements Serializable {

    def script
    def block

    phase(script) {
        this.script = script

        block = stepsBlock.newInstance(script)
        block.steps = "echo test1"

    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

            parallelBlocks['block1'] = {
                script.stage ("test1",block.steps)
            }
            script.parallel parallelBlocks
        }
    }

}

