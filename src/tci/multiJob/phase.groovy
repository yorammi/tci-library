package tci.multiJob;

import tci.multiJob.stepsBlock

@NonCPS
class phase implements Serializable {

    def blocks = [:]

    phase(script) {
        this.script = script

        blocks.values().add(new stepsBlock())
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
            parallel parallelBlocks
        }
    }

}
