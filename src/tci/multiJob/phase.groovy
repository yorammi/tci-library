package tci.multiJob;

import tci.multiJob.stepsBlock

class phase implements Serializable {

    def script
    def block1
    def block2
    def block3

    phase(script) {
        this.script = script

        script.echo "before 1"
        block1 = stepsBlock.newInstance(script)
        script.echo "before 2"
        block2 = stepsBlock.newInstance(script)
        script.echo "before 3"
        block3 = stepsBlock.newInstance(script)
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

            parallelBlocks['1'] = {
                block1
            }
            parallelBlocks['2'] = {
                block2
            }
            parallelBlocks['3'] = {
                block3
            }
            script.echo "before parallel"
            script.parallel parallelBlocks
        }
    }

}

