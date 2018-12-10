package tpl.multiJob;

import tpl.utils.Logger
import com.cloudbees.groovy.cps.NonCPS


class tplMultiJob implements Serializable {

    class job {
        String id = ""
        String title = ""
        String jobName = ""
        String[] parameters = [:]
    }
    class stage {
        String id = ""
        String title = ""
        String genre = ""
    }

    def logger
    def script
    def pauseAfterEachStage

    tplMultiJob(script) {
        this.script = script

        logger = new Logger(script)
    }

    void run() {
        script.timestamps() {
            try {
                runImpl()
            } finally {
                try {
                    if (pauseAfterEachStage) {
                        script.timeout(time: 180, unit: 'MINUTES') {
                            script.input 'Continue to next stage?'
                        }
                    }
                    archiveServiceLogs()
                    cleanupResources()
                    deleteWorkspace()
                    if (firstUnstableStage) {
                        script.echo "Build became UNSTABLE in stage $firstUnstableStage"
                    }
                } catch (all) {
                    logger.info "Issue during cleanup: $all. This can hide real issue that happened in the steps, check what step actually failed"
                }

            }
        }
    }

    void runStage(String name, Closure stage) {
        if (currentBuildResult in ['SUCCESS', null]) {
            script.echo "Start stage $name"
            script.stage(name, stage)
            script.echo "End stage $name with result ${currentBuildResult ?: 'SUCCESS'}"
        } else {
            script.stage(name) {
                script.echo "Build is unstable, skipping stage $name"
                this.firstUnstableStage = firstUnstableStage ?: name
            }
        }
        if (pauseAfterEachStage) {
            script.timeout(time: 180, unit: 'MINUTES') {
                script.input 'Continue to next stage?'
            }
        }
    }

    void runImpl() {
        script.echo "test"
    }

}
