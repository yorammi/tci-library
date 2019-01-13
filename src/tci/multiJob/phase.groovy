package tci.multiJob;

import tci.multiJob.stepsBlock

class phase implements Serializable {

    def script
    def blocks = [:]

    phase(script) {
        this.script = script

        script.echo "before adding block"
        blocks.values().add(new stepsBlock())
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

