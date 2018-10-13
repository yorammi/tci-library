package tpl.services

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
@Grab('org.yaml:snakeyaml:1.17')

import tpl.utils.Logger
class Deployer implements Serializable{

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
    private GString dockerImage

    Deployer(script,featureName,serviceTag,helmRepoURL,helmRepo,helmGitRepo,helmGitRepoBranch,helmCrendetialId,awsCrendetialId,kubeContext) {
        this.script = script
        logger = new Logger(script)
        this.featureName = featureName
        this.service = serviceTag
        this.newVersion = "${baseVersion}.${script.env.BUILD_NUMBER}"
        this.helmRepoURL = helmRepoURL
        this.helmRepo = helmRepo
        this.helmCrendetialId =  helmCrendetialId
        this.awsCrendetialId =  awsCrendetialId
        this.helmGitRepo = helmGitRepo
        this.helmGitRepoBranch = helmGitRepoBranch
        this.kubeContext = kubeContext
        helmPluginUrl = "https://github.com/hypnoglow/helm-s3.git"
    }

    void deploy(){
        script.stage("Deployment env. setup", this.&deploymentEnvSetup)
        script.stage("HELM package", this.&packegeHelm)
        script.stage("HELM dependecy update", this.&helmDependencyUpdate)
        script.stage("HELM deploy", this.&helmDeploy)
        waitTillDeployComplete()
    }

    void packegeHelm(){
        logger.info('packegeHelm')
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/") {
            buildHelm(featureName)
        }

    }

    void buildHelm(it){
        script.dir(it) {
            def myls = script.sh(script: "ls", returnStdout: true)
            script.echo "The Values $myls"
            def valuesYaml = script.readYaml file: 'values.yaml'
            valuesYaml.image.tag = service
            valuesYaml.namespace = featureName
            script.echo "The Values $valuesYaml"
            script.sh "mv values.yaml values.yaml.org"
            script.writeYaml file: 'values.yaml', data: valuesYaml
            upgradeChartVersion()

            def deploymentYaml = script.readYaml file: 'templates/deployment.yml'
            dockerImage = "${script.env.dockerRegisteryPrefix}/${it}:${service}.${script.env.BUILD_NUMBER}"
            script.echo "-----DOCKER IMAGE NAME------ " + dockerImage
            deploymentYaml.spec.template.spec.containers.image = "$dockerImage"


            script.echo "The changed Deployment.yaml $deploymentYaml"
            script.sh "mv templates/deployment.yml templates/deployment.yml.org"
            //script.writeYaml file: 'templates/deployment.yml', data: deploymentYaml
            def txt = yamlToString(deploymentYaml)
            script.echo "----------- YAML STRING THAT WILL BE SAVED TO deployment.yml  -------- \n $txt"
            script.writeFile file: 'templates/deployment.yml', text: txt

            script.sh "helm package ."
            script.sh "helm s3 push --force ./${it}-${newVersion}.tgz ${helmRepo}"
        }
        updateHelmUmbrella(it)
    }

    String yamlToString(Object data) {
        def opts = new DumperOptions()
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        return new Yaml(opts).dump(data)
    }


    void updateHelmUmbrella(chartName){
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/ant-umbrella") {
            def requirementsYaml = script.readYaml file: 'requirements.yaml'
            requirementsYaml.dependencies.each{
                if ( it.name == chartName )
                    it.version = newVersion

            }
            script.sh "mv requirements.yaml requirements.yaml.org"
            script.writeYaml file: 'requirements.yaml', data: requirementsYaml
            pushUmbrellaCode()
        }

    }


    void upgradeChartVersion() {
        def chartYaml = script.readYaml file: 'Chart.yaml'
        chartYaml.version = this.newVersion
        script.sh "mv Chart.yaml Chart.yaml.org"
        script.writeYaml file: 'Chart.yaml', data: chartYaml

    }
    void pushUmbrellaCode(){
        script.echo "push umbrella code"
        //script.withCredentials([script.sshUserPrivateKey(credentialsId: helmCrendetialId, keyFileVariable: 'keyfile')]) {
            script.sh "git config user.email jenkins@tikalk.com"
            script.sh "git config user.name JenkinsOfTikal"
            script.sh "git checkout ${helmGitRepoBranch}"
            script.sh "git add requirements.yaml"
            script.sh "git commit -m 'jenkins update version'"
        script.sshagent([helmCrendetialId]) {
            script.sh "git push -u origin ${helmGitRepoBranch}"
        }
            // script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git push -u origin ${featureName}'"
        //}
    }

    void helmDeploy(){
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/ant-umbrella") {
            script.sh "kubectl config use-context ${kubeContext}"
            script.sh "helm upgrade ant-smasher --set global.namespace=ant-smasher ."
        }

    }
    void helmDependencyUpdate(){
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/ant-umbrella") {
            script.sh "kubectl config use-context ${kubeContext}"
            script.sh "helm dep update ."
        }
    }

    void waitTillDeployComplete(){
        script.timeout(time: 4,unit: 'MINUTES') {
            script.waitUntil {
                try {
                    validateDeployment()
                } catch (ignored) {
                    false
                }
            }
        }
    }

    boolean validateDeployment(){
        script.echo "Waiting for Services to start"
        def podsList = script.sh(script: "kubectl get pods -n $featureName" ,  returnStdout: true).split("\r?\n")
        def services = [:]
        podsList.each { line, count ->
            def index =0
            def name, status
            //logger.info "The Line $line"
            line.tokenize().each{ a ->
                index +=1
                if ( index == 1)
                    name= a
                if ( index == 3 )
                    status= a
            }
            if ( !status.equals('Running') && !status.equals('STATUS') )
                services.put(name, status)
        }
        services.each {
            logger.info " $it"
        }
        return services.isEmpty();
    }

    void deploymentEnvSetup(){
        logger.info "Deployment env. setup"

        script.env.AWS_REGION="eu-west-1"
        script.env.HELM_HOST="AAA"
        script.tplAWSConfigure(awsCrendetialId)

        script.tplRepositoryDirectoryCheckout(helmGitRepo, helmGitRepoBranch, helmCrendetialId, 'kubernetes')

        script.dir("${script.env.WORKSPACE}"){
             script.withCredentials([script.kubeconfigContent(credentialsId: 'kube-config', variable: 'KUBECONFIG_CONTENT')]){
                script.sh "mkdir -p ~/.kube"
                script.sh "echo \"${script.env.KUBECONFIG_CONTENT}\" > /home/jenkins/.kube/config"
                script.sh "kubectl config  current-context"
                script.sh "kubectl config  use-context ${kubeContext}"
                script.sh "helm init --kube-context ${kubeContext}"
                script.sh "helm plugin install ${helmPluginUrl}"
                script.sh "helm repo add ${helmRepo} ${helmRepoURL}"
                script.sh "helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator"
            }
        }
    }
}