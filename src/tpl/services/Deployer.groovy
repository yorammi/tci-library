package tpl.services

import tpl.utils.Logger
class Deployer implements Serializable{

    def log = Logger

    def script
    def logger
    def featureName
    def serviceName
    def service
    def baseVersion = '0.1.0'
    def newVersion
    def kubeContext
    def helmRepoURL
    def helmRepo
    def helmGitRepo
    def helmGitRepoBranch
    def helmCrendetiaslId

    Deployer(script,featureName,serviceTag,helmRepoURL,helmRepo,helmGitRepo,helmGitRepoBranch,helmCrendetiaslId, kubeContext) {
        this.script = script
        logger = new Logger(script)
        this.featureName = featureName
        this.service = serviceTag
        this.newVersion = "${baseVersion}-${featureName}-${script.env.BUILD_NUMBER}"
        this.helmRepoURL = helmRepoURL
        this.helmRepo = helmRepo
        this.helmCrendetiaslId =  helmCrendetiaslId
        this.helmGitRepo = helmGitRepo
        this.helmGitRepoBranch = helmGitRepoBranch
        this.kubeContext = kubeContext
    }

    void deploy(){
        script.stage("k8s checkout", this.&k8sCheckout)
        script.stage("HELM init", this.&helmInit)
        script.stage("HELM package", this.&packegeHelm)
        script.stage("HELM dependecy update", this.&helmDependencyUpdate)
        script.stage("HELM deploy", this.&helmDeploy)
        waitTillDeployComplete()
    }

    void k8sCheckout() {
        script.tplRepositoryDirectoryCheckout(helmGitRepo, helmGitRepoBranch, helmCrendetiaslId, 'kubernetes')
    }

    void packegeHelm(){
        logger.info('packegeHelm')
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/") {
            buildHelm(featureName)
                    // script.sh "cp ${script.env.WORKSPACE}/kubernetes/helm_charts/$it/*.tgz ${script.env.WORKSPACE}/kubernetes/umbrella-chart/charts/"
        }

    }

    void buildHelm(it){
        script.dir(it) {
            def myls = script.sh(script: "ls", returnStdout: true)
            script.echo "The Values $myls"
            def valuesYaml = script.readYaml file: 'values.yaml'
            valuesYaml.image.tag =
//        if ( ! serviceName.startsWith('bc') )
//            valuesYaml.image.repository = "340481513670.dkr.ecr.us-east-1.amazonaws.com/$serviceName"
                    valuesYaml.namespace = featureName
            script.echo "The Values $valuesYaml"
            script.sh "mv values.yaml values.yaml.org"
            script.writeYaml file: 'values.yaml', data: valuesYaml
            upgradeChartVersion()
            //pushCode()
            script.sh "helm package ."
            script.withEnv(["AWS_REGION=eu-west-1"]) {
                script.withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                    script.sh "helm s3 push --force ./${it}-${newVersion}.tgz ${helmRepo}"
                }
            }
        }
        updateHelmUmbrella(it)
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
            //pushUmbrellaCode()
        }

    }


    void upgradeChartVersion() {
        def chartYaml = script.readYaml file: 'Chart.yaml'
        chartYaml.version = this.newVersion
        script.sh "mv Chart.yaml Chart.yaml.org"
        script.writeYaml file: 'Chart.yaml', data: chartYaml

    }
    void pushCode(){
        // script.sh "git checkout -b ${featureName}"
        script.withCredentials([script.sshUserPrivateKey(credentialsId: "15f67460-fb15-4ca2-8e33-84914b1a151d", keyFileVariable: 'keyfile')]) {
            script.sh "git add values.yaml Chart.yaml"
            script.sh "git commit -m 'jenkins update version'"
            script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git push --set-upstream origin HEAD:${featureName}'"
        }
    }

    void pushUmbrellaCode(){
        script.echo "push umbrella code"
        script.withCredentials([script.sshUserPrivateKey(credentialsId: "gitsshkey", keyFileVariable: 'keyfile')]) {
            script.sh "git add requirements.yaml"
            script.sh "git commit -m 'jenkins update version'"
            script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git push -u origin ${featureName}'"
        }
    }

    void helmDeploy(){
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/ant-umbrella"){
            script.withEnv(["AWS_REGION=us-east-1"]) {
                script.sh "kubectl config use-context ${kubeContext}"
                script.sh "helm upgrade $featureName --set global.namespace=$featureName,global.stack=$featureName,global.database=bc-$featureName-psql.ano-dev.com ."
            }
            // aws rds --region us-east-1 describe-db-instances


            //--set global.namespace=${BRANCHNAME},global.stack=${BRANCHNAME},global.database=$ENDPOINT ./kubernetes/umbrella-chart/
        }

    }
    void helmDependencyUpdate(){
        script.dir("${script.env.WORKSPACE}/kubernetes/helm/ant-umbrella") {
            script.withEnv(["AWS_REGION=us-east-1"]) {
                script.sh "kubectl config use-context ${kubeContext}"
                script.withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                    script.sh "helm repo add ${helmRepo} ${helmRepoURL}"
                    script.sh "helm dep update ."
                }
            }
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

    void helmInit(){
        logger.info "in init"
        script.dir("${script.env.WORKSPACE}"){
             script.withEnv(["HELM_HOST=AAA", "AWS_REGION=eu-west-1"]) {
                   // script.withCredentials([script.file(credentialsId: 'kube-config', variable: 'FILE')]) {
                 script.withCredentials([script.kubeconfigContent(credentialsId: 'kube-config', variable: 'KUBECONFIG_CONTENT')]){
                        script.sh "mkdir -p ~/.kube"
                        script.sh "echo \"${script.env.KUBECONFIG_CONTENT}\" > /home/jenkins/.kube/config"
                        script.sh "kubectl config  current-context"
                        script.sh "kubectl config  use-context ${kubeContext}"
                        script.sh "helm init --kube-context ${kubeContext}"
                        script.sh "helm plugin install https://github.com/hypnoglow/helm-s3.git"
                        script.withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                            script.sh "helm repo add ${helmRepo} ${helmRepoURL}"
                        }

                    }
                }
            }

    }
}