package tci.jobs.ci;

import tci.jobs.baseJob

class baseMavenCIJob extends baseJob {

    boolean checkout = false
    def repoUrl = ""
    def branch = ""
    def subDir = "."
    boolean setVersion = false
    def version ="1.0"
    boolean deploy = false
    def additionalVersionCmd = ""
    def additionalCompileCmd = ""
    def additionalTestsCmd = ""
    def additionalPackageCmd = ""
    def additionalDeployCmd = ""

    def revision
    boolean isMavenBuild

    baseMavenCIJob(script) {
        super(script)
    }

    @Override
    void runImpl() {
        runStage('Setup', this.&setup)
        runStage('Code Checkout', this.&checkout)
        if(setVersion)
        {
            runStage('Set version', this.&setVersion)
        }
        runStage('Code compile', this.&compile)
        runStage('Package', this.&pack)
        runStage('Unit Tests', this.&unitTests)
    }

    void setup() {
        script.tciEnv.initEnv()
    }

    void checkout() {
        script.checkout script.scm
        if(checkout) {
            script.tciGit.gitCheckout(repoUrl: repoUrl, branch:branch, toDir: subDir)
        }
        script.currentBuild.displayName += " [${env.GIT_URL}:${env.GIT_LOCAL_BRANCH}]"
    }

    void setVersion() {
        script.tciMaven.setVersion(version:version, dir:subDir, additionalCmd:additionalVersionCmd)
    }

    void compile() {
        script.tciMaven.mavenCompile(dir:subDir, additionalCmd:additionalCompileCmd)
    }

    void unitTests() {
        script.tciMaven.mavenRunUnitTests(dir:subDir, additionalCmd:additionalTestsCmd)
    }

    void pack() {
        if (deploy) {
            script.tciMaven.mavenDeploy(dir:subDir, additionalCmd:additionalPackageCmd)
        }
        else {
            script.tciMaven.mavenPackage(dir:subDir, additionalCmd:additionalDeployCmd)
        }

    }
}
