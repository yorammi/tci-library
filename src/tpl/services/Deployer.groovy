package tpl.services

@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.*

import tpl.utils.Logger

class Deployer implements Serializable {

    def log = Logger

    def script
    def logger
    def featureName
    def serviceName
    def service
    def baseVersion = '0.1'
    def newVersion
    def kubeContext
    def helmRepoURL
    def helmRepo
    def helmGitRepo
    def helmGitRepoBranch
    def helmCrendetialId
    def awsCrendetialId
    private String helmPluginUrl
    private String dockerImage

    Deployer(script, featureName, serviceTag, helmRepoURL, helmRepo, helmGitRepo, helmGitRepoBranch, helmCrendetialId, awsCrendetialId, kubeContext) {
        this.script = script
        logger = new Logger(script)
        this.featureName = featureName
        this.service = serviceTag
        this.newVersion = "${baseVersion}.${script.env.BUILD_NUMBER}"
        this.helmRepoURL = helmRepoURL
        this.helmRepo = helmRepo
        this.helmCrendetialId = helmCrendetialId
        this.awsCrendetialId = awsCrendetialId
        this.helmGitRepo = helmGitRepo
        this.helmGitRepoBranch = helmGitRepoBranch
        this.kubeContext = kubeContext
        helmPluginUrl = "https://github.com/hypnoglow/helm-s3.git"
    }

    void deploy() {
        script.stage("Deployment env. setup", this.&deploymentEnvSetup)
        //script.stage("HELM package", this.&packegeHelm)
        script.stage("HELM dependecy update", this.&helmDependencyUpdate)
        script.stage("HELM deploy", this.&helmDeploy)
        waitTillDeployComplete()
    }

    void packegeHelm() {
        logger.info('packegeHelm')
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/") {
            buildHelm(featureName)
        }

    }

    void buildHelm(it) {
        script.dir(it) {
            def valuesYaml = script.readYaml file: 'values.yaml'
            script.echo "=============== Values before build =====================\n" + yamlToString(valuesYaml)
            valuesYaml.image.tag = "${service}.${script.env.BUILD_NUMBER}".toString()
            valuesYaml.namespace = featureName.toString()
            script.sh "mv values.yaml .values.yaml.org"
            script.writeYaml file: 'values.yaml', data: valuesYaml

            // add build number to chart version
            upgradeChartVersion()

            // change the docker image this chart uses (templates/deplyoment.yaml:containers.image)
            updateHelmDeploymentImage(featureName)

            def updatedValues = script.readYaml file: 'values.yaml'
            script.echo "=============== New Values =====================\n" + yamlToString(updatedValues)
            script.sh "helm package ."
            script.sh "helm s3 push --force ./${it}-${newVersion}.tgz ${helmRepo}"
        }
        updateHelmUmbrella(it)
    }

    def updateHelmDeploymentImage(it) {
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/${it}") {
            def valuesYaml = script.readYaml file: 'values.yaml'
            valuesYaml.image.tag = "${service}.${script.env.BUILD_NUMBER}".toString()
            script.sh "mv values.yaml values.yaml.org"
            script.writeYaml file: 'values.yaml', data: valuesYaml
        }

    }

    static String yamlToString(Object data) {
        def opts = new DumperOptions()
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        return new Yaml(opts).dump(data)
    }


    void updateHelmUmbrella(chartName) {
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/ant-umbrella") {
            def requirementsYaml = script.readYaml file: 'requirements.yaml'
            script.echo "==========   Requirements Before Build  ==============\n" + yamlToString(requirementsYaml)
            requirementsYaml.dependencies.each {
                if (it.name == chartName)
                    it.version = newVersion.toString()
            }
            script.sh "mv requirements.yaml requirements.yaml.org"
            script.echo "==========   New Requirements  ==============\n" + yamlToString(requirementsYaml)
            script.writeYaml file: 'requirements.yaml', data: requirementsYaml
            pushUmbrellaCode()
        }

    }


    void upgradeChartVersion() {
        def chartYaml = script.readYaml file: 'Chart.yaml'
        script.echo "----- Chart before build ------\n" + yamlToString(chartYaml)
        chartYaml.version = this.newVersion.toString()
        script.sh "mv Chart.yaml Chart.yaml.org"
        script.echo "------ New Chart -------\n" + yamlToString(chartYaml)
        script.writeYaml file: 'Chart.yaml', data: chartYaml

    }

    void pushUmbrellaCode() {
        script.echo "Pushing ant-umbrella code"
        script.sh "git config user.email jenkins@tikalk.com"
        script.sh "git config user.name JenkinsOfTikal"
        script.sh "git checkout ${helmGitRepoBranch}"
        script.sh "git add requirements.yaml ../$featureName/values.yaml"
        script.sh "git commit -m 'jenkins update version component [${script.env.JOB_NAME}] build [${script.env.BUILD_NUMBER}]'"
        script.sshagent([helmCrendetialId]) {
            script.sh "git push -u origin ${helmGitRepoBranch}"
        }
    }

    void helmDeploy() {
        script.dir("${script.env.WORKSPACE}/ghost") {
            script.sh "kubectl config use-context ${kubeContext}"
            script.sh "helm upgrade ant-smasher ."
        }

    }

    void helmDependencyUpdate() {
        script.dir("${script.env.WORKSPACE}/ghost") {
            script.sh "kubectl config use-context ${kubeContext}"
            script.sh "helm dep update ."
        }
    }

    void waitTillDeployComplete() {
        script.timeout(time: 4, unit: 'MINUTES') {
            script.waitUntil {
                try {
                    validateDeployment()
                } catch (ignored) {
                    false
                }
            }
        }
    }

    boolean validateDeployment() {
        script.echo "Waiting for Services to start"
        def podsList = script.sh(script: "kubectl get pods -n $featureName", returnStdout: true).split("\r?\n")
        def services = [:]
        podsList.each { line, count ->
            def index = 0
            def name, status
            //logger.info "The Line $line"
            line.tokenize().each { a ->
                index += 1
                if (index == 1)
                    name = a
                if (index == 3)
                    status = a
            }
            if (!status.equals('Running') && !status.equals('STATUS'))
                services.put(name, status)
        }
        services.each {
            logger.info " $it"
        }
        return services.isEmpty();
    }

    void deploymentEnvSetup() {
        logger.info "Deployment env. setup"

        script.env.AWS_REGION = "eu-west-1"
        script.env.HELM_HOST = "AAA"
//        script.tplAWSConfigure(awsCrendetialId)

//        script.tplRepositoryDirectoryCheckout(helmGitRepo, helmGitRepoBranch, helmCrendetialId, 'kubernetes')

        script.dir("${script.env.WORKSPACE}") {
            script.withCredentials([script.kubeconfigContent(credentialsId: 'kube-config', variable: 'KUBECONFIG_CONTENT')]) {
                script.sh "mkdir -p ~/.kube"
                script.sh "echo \"${script.env.KUBECONFIG_CONTENT}\" > /home/jenkins/.kube/config"
                script.sh "kubectl config  current-context"
                script.sh "kubectl config  use-context ${kubeContext}"
                script.sh "helm init --kube-context ${kubeContext}"
           //     script.sh "helm plugin install ${helmPluginUrl}"
          //      script.sh "helm repo add ${helmRepo} ${helmRepoURL}"
                script.sh "helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator"
            }
        }
    }
}