package tci.multiJob;

import tci.multiJob.stepsBlock

class phase implements Serializable {

    def script
    def blocks = new stepsBlock[3]

    phase(script) {
        this.script = script

        blocks[0] = new stepsBlock()
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

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
            script.parallel parallelBlocks
        }
    }

}

