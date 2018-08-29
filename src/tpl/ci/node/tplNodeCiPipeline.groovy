package tpl.ci.node
import tpl.ci.tplBaseCiPipeline
import tpl.services.Deployer

class tplNodeCiPipeline extends tplBaseCiPipeline{

 tplNodeCiPipeline(script){
        super(script)
 }

@Override
    void setup() {
        gitConfig()


        // automatically capture environment variables while downloading and uploading files
    }

    
@Override
    void checkout() {
        script.checkout script.scm
    }
 @Override
    void build() {


        script.dir("${script.env.WORKSPACE}") {
           // script.withCredentials([script.usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'DOCKER_REGISTRY_PASS', usernameVariable: 'DOCKER_REGISTRY_USER')]) {

//                def nodeHome =script.tool 'NodeJS10'
//                script.sh "export PATH=\${PATH}/${nodeHome}/bin; npm install"
//                script.sh "${nodeHome}/bin/npm run build"
                    script.docker.withRegistry('https://index.docker.io/v1','dockerHub') {
                        def customImage = script.docker.build("tikal/web-ui:${script.env.BUILD_NUMBER}")
                        /* Push the container to the custom Registry */
                        customImage.push()
                    }


            //}

        }
            
    }     
@Override
    void deploy() {
        logger.info "Helm Deploy"
        def deployer = new Deployer(script,script.scm.branchName,"${script.env.JOB_NAME}-${script.env.BUILD_NUMBER}")
        deployer.deploy()
    }



    void gitConfig() {
   }

    void gitCommit() {

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

