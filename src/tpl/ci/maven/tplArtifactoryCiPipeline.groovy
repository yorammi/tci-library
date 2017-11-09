package tpl.ci.maven

import tpl.ci.tplBaseCiPipeline
import org.jfrog.hudson.pipeline.dsl.ArtifactoryPipelineGlobal
import org.jfrog.hudson.pipeline.types.MavenDescriptor

/**
 * Created by ccohen on 7/6/17.
 */
class tplArtifactoryCiPipeline extends tplBaseCiPipeline {

    ArtifactoryPipelineGlobal artifactory
//    MavenModuleSet moduleSet
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

    tplArtifactoryCiPipeline(script) {
        super(script)
        artifactory = new ArtifactoryPipelineGlobal(script)

    }

    @Override
    void runImpl() {
//        super.runImpl()
        try {
            runStage('Setup', this.&setup)
            runStage('Checkout', this.&checkout)

            if (release) {
                preRelease()
//                doRelease()
            }

            runStage('Test', this.&unitTests)
            runStage('Install', this.&build)
            runStage('Deploy', this.&deploy)

            if (release) {
                postRelease()
            }

        } catch (e) {
            script.currentBuild.result = "FAILURE"
//            script.error e.message
            throw e
        }
        finally {
            buildNotifier()
        }

    }

    @Override
    void setup() {
        gitConfig()

        server = artifactory.server artifactoryName

        rtMaven = artifactory.newMavenBuild()
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
        script.git credentialsId: gitCredentialsId, url: gitRepoUrl
    }

    @Override
    void unitTests() {
        rtMaven.run pom: 'pom.xml', goals: 'clean test'

        script.junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml, target/failsafe-reports/*.xml'
    }

    @Override
    void build() {
        rtMaven.run pom: 'pom.xml', goals: 'install', buildInfo: buildInfo
    }

    @Override
    void deploy() {
        buildInfo.env.collect() // collect environment variables
        buildInfo.retention maxBuilds: 10, maxDays: 7, deleteBuildArtifacts: true
        rtMaven.deployer.deployArtifacts buildInfo
        server.publishBuildInfo buildInfo

        script.archive "target/**/*"
    }

    void doRelease() {
        def version = computeReleaseVersion()
//        script.sh '''
//            eval `ssh-agent -s`
//            ssh-add '''

//        script.steps.step.sshagent([gitCredentialsId]) {
//            rtMaven.run pom: 'pom.xml', goals: '-Dresume=false release:prepare release:perform'
//        }

    }

    void preRelease() {
        // omit '-SNAPSHOT'
        // def version = computeReleaseVersion()
        def version = releaseVersion
        logger.info 'prepare to release version: ' + version

        script.pom = script.readMavenPom file: 'pom.xml'
        MavenDescriptor descriptor = artifactory.mavenDescriptor()
        descriptor.setVersion(version)
        descriptor.setFailOnSnapshot(true)
        descriptor.transform()

        computeScmTag(script.pom.artifactId + '-' + version)

        script.sh "git commit -am 'prepare release ${script.pom.artifactId}-$version'"
    }

    void gitConfig() {
/*
        script.sh '''
            git config user.name Jenkins
            git config user.email nobody@domain.com'''
*/
    }

    void gitCommit() {

    }

    void postRelease() {
        // set version to next version + '-SNAPSHOT'
        // def version = computeNextVersion()
        def version = developmentVersion
        logger.info 'set next development version: ' + version

        MavenDescriptor descriptor = artifactory.mavenDescriptor()
        descriptor.setVersion(version)
        descriptor.setFailOnSnapshot(false)
        descriptor.transform()

        // set scm/tag to HEAD
        computeScmTag('HEAD')

        script.sh "git commit -am 'prepare for next development iteration'"

        // update buildinfo, icon and released version

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
