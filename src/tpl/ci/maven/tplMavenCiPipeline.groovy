package tpl.ci.maven

import tpl.ci.tplBaseCiPipeline
import tpl.services.Deployer

/**
 * Created by ccohen on 7/6/17.
 */
class tplMavenCiPipeline extends tplBaseCiPipeline {

    def server
    def rtMaven
    def buildInfo

    def artifactoryName
    def mavenName
    def snapshotRepo
    def releaseRepo
    def vSnapshotRepo
    def vReleaseRepo

    def gitRepoUrl
    def gitCredentialsId

    boolean dryRun = false
    boolean release = false
    String releaseVersion
    String developmentVersion

    tplMavenCiPipeline(script) {
        super(script)

    }

    @Override
    void runImpl() {
        try {
            runStage('Setup', this.&setup)
            runStage('Checkout', this.&checkout)
            runStage('Test', this.&unitTests)
            runStage('Install', this.&build)
            runStage('Deploy', this.&deploy)
        } catch (e) {
            script.currentBuild.result = "FAILURE"
            throw e
        }
        finally {
            buildNotifier()
        }

    }

    @Override
    void setup() {
        gitConfig()


        rtMaven.tool = mavenName // Tool name from Jenkins configuration
        rtMaven.resolver releaseRepo: vReleaseRepo, snapshotRepo: vSnapshotRepo, server: server
        rtMaven.deployer releaseRepo: releaseRepo, snapshotRepo: snapshotRepo, server: server
        rtMaven.deployer.deployArtifacts = false // Disable artifacts deployment during Maven run

        buildInfo = artifactory.newBuildInfo()
        buildInfo.env.capture = true
        // automatically capture environment variables while downloading and uploading files
    }

    @Override
    void checkout() {
        script.checkout script.scm
    }

    @Override
    void unitTests() {
        rtMaven.run pom: 'pom.xml', goals: 'clean test'

        script.junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml, target/failsafe-reports/*.xml'
    }

    @Override
    void build() {
        script.dir("${script.env.WORKSPACE}") {
            script.withMaven(
                    maven: 'maven-3.3.9',
                    jdk: 'oracle-jdk-8',
                    mavenSettingsFilePath: script.env.MAVEN_SETTINGS,
                    mavenLocalRepo: '.repository')
                    {
                        script.withEnv(["SERVICE_TAG=${serviceTag}"]) {
                            script.sh "mvn --projects ${serviceToBuild} -P docker --also-make clean install"
                        }
                    }
        }


    }
    @Override
    void deploy() {
        logger.info "Helm Deploy"
        def deployer = new Deployer(script,branchName,serviceToBuild,serviceTag,chrat2Deploy)
        deployer.deploy()
    }



    void gitConfig() {
   }

    void gitCommit() {

    }


    String computeReleaseVersion() {
        logger.info 'compute release version'

        script.pom = script.readMavenPom file: 'pom.xml'
        String version = script.pom.version
        try {
            if (version.endsWith('-SNAPSHOT')) {
                version = version.substring(0, version.length() - "SNAPSHOT".length() - 1)
            } else {
                script.error 'failed to compute release version'
            }
        } catch (e) {
            logger.error e.message
        }

        return version
    }

    String computeNextVersion() {
        String version = script.pom.version
        try {
//            DefaultVersionInfo dvi = new DefaultVersionInfo(version)
//            version = dvi.getNextVersion().getSnapshotVersionString()


            logger.info 'next development version: ' version
        } catch (e) {
            logger.info e.message
        }

        return version
    }

    void computeScmTag(String tag) {
        // set scm/tag to '[name]-[version]'
        script.pom.scm.tag = tag
        script.writeMavenPom model: script.pom
    }

    void buildNotifier() {

        def subject = script.env.JOB_NAME + ' - Build #' + script.currentBuild.number + ' - ' + script.currentBuild.currentResult
        script.emailext(
                to: 'user@domain.com',
                subject: subject,
                body: script.env.BUILD_URL
//                attachLog: true,
                //recipientProviders: [[$class: 'DevelopersRecipientProvider']]
        )
    }
}
