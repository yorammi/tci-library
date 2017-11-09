//package tpl.ci;

import tpl.utils.Logger
import com.cloudbees.groovy.cps.NonCPS

abstract class tpl_BaseCiPipeline implements Serializable {

    def logger
    def script
    def gitBranch
    def gitCredentialsId
    def gitRepoUrl
    def firstUnstableStage
    def pauseAfterEachStage

    tpl_BaseCiPipeline(script) {
        this.script = script

        logger = new Logger(script)
    }

    void run() {
        script.timestamps() {
            try {
                script.echo "Running with params: ${this.properties}"
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
        runStage('Setup', this.&setup)
        runStage('Checkout', this.&checkout)
        runStage('Build', this.&build)
        runStage('Prepare Test Env', this.&prepareTestEnv)
        runStage('System Tests', this.&systemTests)
        runStage('Deploy', this.&deploy)

    }

    void setup() {
        initParams()
        populateBuildInfo()
        setGitConfig()
    }

    void initParams() {
        gitCredentialsId = script.params.gitCredentialsId //script.params.// Implement to set params that are not able to set in constructor (due to @NonCPS etc)
        gitRepoUrl  = script.params.gitRepoUrl
    }

    void populateBuildInfo() {
//        populateBuildDisplayName()
//        populateBuildDescription()
    }

    void populateBuildDisplayName() {
        script.currentBuild.displayName = "" //"""${script.currentBuild.displayName} ${gitBranch ?: ''}"
    }

    void populateBuildDescription() {
        // Set public IP as description, if found
        String description = "Need the slave ip"
        script.currentBuild.description = description

    }

    void deploy(){
        logger.info "Implements deploy logic here (push to docker , maven, gradle deploy)"
    }

    void setGitConfig() {
        script.sh '''git --version
        git config --global push.default simple
        git config --global user.email "aaa"
        git config --global user.name "bbb"
        '''
    }

    void checkout() {
        script.git credentialsId: gitCredentialsId, url: gitRepoUrl

    }

    void gitCheckout(Map m) {
        def repository = m.repository
        String branch = m.branch ?: 'master'
        def targetDir = m.targetDir ?: m.repository

        script.dir("$targetDir") {
            // Run git clean and swallow errors which are normal when running first time
            script.sh returnStatus: true, script: 'git fetch'
            script.git credentialsId: 'jenkins-github-ssh', url: "git@.git", branch: "$branch"
        }
    }

    void gitCheckoutProject() {
        gitCheckout branch: gitBranch, repository: gitRepository
    }

    // Rebase the current change over current origin master, so we are running with latest changes
    void gitRebaseOntoMaster() {
        script.dir(gitRepository) {
            script.sh "git fetch && git pull origin $masterBranch"
        }
    }

    void build() {
    }

    void createGitInfoFile() {
        // Implement in CIs where info file is not created during compile (i.e. non maven builds
    }

    void compile(Map m) {
        // Implement in CIs where build is needed
    }

    void unitTests() {
        // Implement in CIs where UTs are not being run during compile (i.e. non maven builds)
    }

    void systemTests() {}

    void prepareTestEnv() {
    }

    void waitForService(Map m) {
        script.echo "Waiting for ${m.name} to start"
        script.timeout(time: m.timeoutSeconds, unit: 'SECONDS') {
            script.waitUntil {
                try {
                    script.httpRequest(m.url)
                    true
                } catch (ignored) {
                    false
                }
            }
        }
        script.echo "${m.name} started succesfully"
    }

    @NonCPS
    static String toPropertiesFile(Map properties) {
        properties.findAll { it.value }
                .collect { "${it.key}=${it.value}" }
                .join('\n')
    }

    void runTests() {
        runSystemTests()
        archiveTestScreenshots()
        archiveTestResults()
        // Archiving test results can change script.currentBuilt.result

    }

    void runSystemTests() {

    }

    void archiveTestScreenshots() {
    }

    void archiveServiceLogs() {
    }

    void cleanupResources() {
    }

    void deleteWorkspace() {
        script.step([$class: 'WsCleanup'])
    }

    void archiveTestResults() {
        // Publishing testNG results
        script.step([$class     : 'JUnitResultArchiver', allowEmptyResults: true,
                     testResults: "**/target/failsafe-reports/junitreports/TEST-*.xml"])
    }

    void gitMergeMaster() {
        script.echo 'Checkout master and merge the feature branch to it'
        script.dir(gitRepository) {
            script.sh """
                git fetch
                git checkout -B $masterBranch origin/$masterBranch
                git merge origin/$gitBranch
            """
        }
    }

    void gitPushToMaster() {
        script.dir(gitRepository) {
            script.sh "git push origin HEAD:${masterBranch ?: 'master'}"
        }
    }

    void gitDeleteRemoteBranch() {
        if (['master', 'stable', 'development'].contains(gitRepository)) {
            script.echo "Not deleting $gitRepository since it is protected"
        }
        script.dir(gitRepository) {
            script.sh "git fetch && git push origin --delete $gitBranch"
        }
    }

    String getCurrentBuildResult() {
        return script.currentBuild.result
    }
}
