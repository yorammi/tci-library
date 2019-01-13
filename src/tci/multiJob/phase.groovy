package tci.multiJob;

import tci.multiJob.stepsBlock

class phase implements Serializable {

    def script
    def blocks = new stepsBlock[3]

    phase(script) {
        this.script = script

        script.echo "before adding block"
        blocks[0] = new stepsBlock(this)
        script.echo "after adding block"
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

            script.echo "before blocks loop"
            blocks.each {
                parallelBlocks[it] = {
                    stage(it) {
                        it.each {
                            steps[itStep] = {
                                script.echo itStep
                            }
                        }
                    }
                }
            }
            parallel parallelBlocks
        }
    }

}

