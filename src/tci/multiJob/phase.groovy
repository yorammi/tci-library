package tci.multiJob;

import tci.multiJob.stepsBlock

class phase implements Serializable {

    def script
    def block1
    def block2
    def block3

    phase(script) {
        this.script = script

        block1 = stepsBlock.newInstance(script)
        block2 = stepsBlock.newInstance(script)
        block3 = stepsBlock.newInstance(script)
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

            parallelBlocks['1'] = {
                script.echo block1.step1
            }
            parallelBlocks['2'] = {
                script.echo block2.step2
            }
            parallelBlocks['3'] = {
                script.echo block3.step3
            }
            script.parallel parallelBlocks
        }
    }

}

