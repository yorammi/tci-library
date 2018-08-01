package tpl.services

import tpl.utils.Logger
class Deployer implements Serializable{

    def log = Logger

    def script
    def logger
    def featureName
    def serviceName
    def tag
    def baseVersion = '0.1.0'
    def newVersion

    Deployer(script,featureName,serviceToBuild,serviceTag,chart2Deploy) {
        this.script = script
        logger = new Logger(script)
        this.featureName=featureName
        this.serviceName=serviceToBuild
        this.tag=serviceTag
        this.newVersion = "${baseVersion}-${featureName}-${script.env.BUILD_NUMBER}"
        this.chart2Deploy=chart2Deploy
    }


    void checkoutSCM(){
        script.checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'kubernetes']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '15f67460-fb15-4ca2-8e33-84914b1a151d', url: 'git@bitbucket.org:anodotengineering/kubernetes.git']]])
        script.dir("${script.env.WORKSPACE}/kubernetes" ) {
            script.withCredentials([script.sshUserPrivateKey(credentialsId: "15f67460-fb15-4ca2-8e33-84914b1a151d", keyFileVariable: 'keyfile')]) {

                boolean remoteBranchExist = script.sh(returnStdout: true, script: "ssh-agent bash -c 'ssh-add $script.keyfile ; git ls-remote --heads git@bitbucket.org:anodotengineering/kubernetes.git ${featureName} | wc -l'").toBoolean()
                if (remoteBranchExist)
                    script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git checkout --track -b ${featureName}  origin/${featureName}'"
                else
                    script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git checkout -b ${featureName}'"

                //merge master to branch for case some one else update the master
                // to get the latest
                script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git fetch origin'"
                script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git merge -X theirs origin/master'"
                //  script.sh "git pull"
            }
        }

    }

    void packegeHelm(){
        logger.info('packegeHelm')
        logger.info('Test vova'+serviceName)
        logger.info('The Charts '+ files)
        script.dir("${script.env.WORKSPACE}/src/main/helm/$it") {
            buildHelm(it)
                    // script.sh "cp ${script.env.WORKSPACE}/kubernetes/helm_charts/$it/*.tgz ${script.env.WORKSPACE}/kubernetes/umbrella-chart/charts/"
        }

    }

    void buildHelm(it){
        def valuesYaml = script.readYaml file: 'values.yaml'
        valuesYaml.image.tag = tag
//        if ( ! serviceName.startsWith('bc') )
//            valuesYaml.image.repository = "340481513670.dkr.ecr.us-east-1.amazonaws.com/$serviceName"
        valuesYaml.namespace = featureName
        script.echo "The Values $valuesYaml"
        script.sh "mv values.yaml values.yaml.org"
        script.writeYaml file: 'values.yaml', data: valuesYaml
        upgradeChartVersion()
        pushCode()
        script.sh "helm package ."
        script.withEnv(["AWS_REGION=us-east-1"]) {
            script.sh "helm s3 push --force ./${it}-${newVersion}.tgz anodot"
        }
        updateHelmUmbrella(it)
    }


    void updateHelmUmbrella(chartName){
        script.dir("${script.env.WORKSPACE}/kubernetes/umbrella-chart") {
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
    void pushCode(){
        // script.sh "git checkout -b ${featureName}"
        script.withCredentials([script.sshUserPrivateKey(credentialsId: "15f67460-fb15-4ca2-8e33-84914b1a151d", keyFileVariable: 'keyfile')]) {
            script.sh "git add values.yaml Chart.yaml"
            script.sh "git commit -m 'jenkins update version'"
            script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git push --set-upstream origin HEAD:${featureName}'"
        }
    }

    void pushUmbrellaCode(){
        script.withCredentials([script.sshUserPrivateKey(credentialsId: "15f67460-fb15-4ca2-8e33-84914b1a151d", keyFileVariable: 'keyfile')]) {
            script.sh "git add requirements.yaml"
            script.sh "git commit -m 'jenkins update version'"
            script.sh "ssh-agent bash -c 'ssh-add $script.keyfile ;git push -u origin ${featureName}'"
        }
    }

    void helmDeploy(){
        script.dir("${script.env.WORKSPACE}/kubernetes/umbrella-chart"){
            script.withEnv(["AWS_REGION=us-east-1"]) {
                script.sh "kubectl config use-context k8s-features.anodot.com"
                script.sh "helm upgrade $featureName --set global.namespace=$featureName,global.stack=$featureName,global.database=bc-$featureName-psql.ano-dev.com ."
            }
            // aws rds --region us-east-1 describe-db-instances


            //--set global.namespace=${BRANCHNAME},global.stack=${BRANCHNAME},global.database=$ENDPOINT ./kubernetes/umbrella-chart/
        }

    }
    void helmDependencyUpdate(){
        script.dir("${script.env.WORKSPACE}/kubernetes/umbrella-chart") {
            script.withEnv(["AWS_REGION=us-east-1"]) {
                script.sh "kubectl config use-context k8s-features.anodot.com"
                script.sh "helm dep update ."
            }
        }
    }

    void deploy(){
        checkoutSCM()
        helmInit()
        packegeHelm()
        helmDependencyUpdate()
        helmDeploy()
        waitTillDeployComplete()
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
        try {
            script.dir("${script.env.WORKSPACE}") {
                script.withEnv(["HELM_HOST=jenkins-ci.anodot.com", "AWS_REGION=us-east-1"]) {
                    script.sh "mkdir -p /home/ubuntu/.kube"
                    script.sh "cp ${script.env.WORKSPACE}/kubernetes/config /home/ubuntu/.kube/"
                    script.sh "kubectl config use-context k8s-features.anodot.com"
                    script.sh "helm init --kube-context k8s-features.anodot.com"
                    script.sh "helm plugin install https://github.com/hypnoglow/helm-s3.git"
                    script.sh "helm repo add anodot s3://anodot-helm-charts/charts"
                }
            }
        }catch(e){}
    }
}