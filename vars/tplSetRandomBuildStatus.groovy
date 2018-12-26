#!/usr/bin/groovy

@NonCPS
def call() {
    tciGeneral.deprecatedMessage("tplSetRandomBuildStatus","tciJobs.setRandomBuildStatus")
    tciGeneral.setRandomBuildStatus()
}
