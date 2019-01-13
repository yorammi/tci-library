package tci.multiJob;

import tci.multiJob.stepsBlock

class phase implements Serializable {

    def script
    def blocks = new stepsBlock[3]

    phase(script) {
        this.script = script

        script.echo "before 1"
        blocks[0] = stepsBlock.newInstance()
        script.echo "before 2"
        blocks[1] = stepsBlock.newInstance()
        script.echo "before 3"
        blocks[2] = stepsBlock.newInstance()
    }

    void run() {
        script.timestamps() {
            def parallelBlocks = [:]

            script.echo "before loop"
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
            script.echo "before parallel"
            script.parallel parallelBlocks
        }
    }

}

