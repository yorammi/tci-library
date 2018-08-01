package tpl.ci.node
import tpl.ci.tplBaseCiPipeline


class tplNodeCiPipeline extends tplBastplMavenCiPipeline{


 @Override
    void build() {


        script.dir("${script.env.WORKSPACE}") {
            script.withCredentials([script.usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'DOCKER_REGISTRY_PASS', usernameVariable: 'DOCKER_REGISTRY_USER')]) {

                
                script.sh "npm install"
                script.sh "npm run build"
                script.docker.withRegistry('https://index.docker.io/v1/tikalk','dockerHub') {
                        def customImage = script.docker.build("${script.env.DOCKER_REPOSITORY}","./docker/src/${script.env.DOCKER_GROUP}/${script.env.DOCKER_REPOSITORY}")
                        /* Push the container to the custom Registry */
                        customImage.push()
                    }
        
    
            }

        }
            
    }     



}

