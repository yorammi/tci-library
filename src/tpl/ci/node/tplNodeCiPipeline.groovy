package tpl.ci.node
import tpl.ci.tplBaseCiPipeline
import tpl.services.Deployer

class tplNodeCiPipeline extends tplBaseCiPipeline{
    def containerName
    def dockerRegisteryPrefix
    def containerTag
    def dockerRegisteryUrl
    def dockerPath
    def helmRepoURL
    def helmRepo
    def helmGitRepo
    def helmGitRepoBranch
    def helmCrendetiaslId
 tplNodeCiPipeline(script){
        super(script)

 }

@Override
void initParams(){
    //super.initParams()
    containerName = script.params.containerName;
    dockerRegisteryPrefix = script.params.dockerRegisteryPrefix;
    containerTag = script.params.containerTag;
    dockerRegisteryUrl= (script.params.get('dockerRegisteryUrl') == null ) ? 'https://index.docker.io/v1' : script.params.get('dockerRegisteryUrl')
    dockerPath =  (script.params.get('dockerPath') == null ) ? '' : script.params.get('dockerPath')
    helmRepoURL = (script.params.get('helmRepoURL') == null ) ? 'Missing Helm Repo Url param' : script.params.get('helmRepoURL')
    helmRepo = (script.params.get('helmRepo') == null ) ? 'Missing Helm Repo param' : script.params.get('helmRepo')
    helmGitRepo = (script.params.get('helmGitRepo') == null ) ? 'Missing Helm Git Repo param' : script.params.get('helmGitRepo')
    helmGitRepoBranch = (script.params.get('helmGitRepoBranch') == null ) ? 'Missing Helm Git Repo branch param' : script.params.get('helmGitRepoBranch')
    helmCrendetiaslId = (script.params.get('helmCrendetiaslId') == null ) ? 'Missing Helm-Jenkins Credentials id' : script.params.get('helmCrendetiaslId')
  }

@Override
    void setup() {
        gitConfig()
        initParams()


        // automatically capture environment variables while downloading and uploading files
    }

    
@Override
    void checkout() {
        script.checkout script.scm
    }
 @Override
    void build() {


        script.dir("${script.env.WORKSPACE}/${dockerPath}") {
          script.withCredentials([script.usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'DOCKER_REGISTRY_PASS', usernameVariable: 'DOCKER_REGISTRY_USER')]) {

//                def nodeHome =script.tool 'NodeJS10'
//                script.sh "export PATH=\${PATH}/${nodeHome}/bin; npm install"
//                script.sh "${nodeHome}/bin/npm run build"
              script.sh "docker login -u ${script.env.DOCKER_REGISTRY_USER} -p ${script.env.DOCKER_REGISTRY_PASS}"
              script.sh "docker build -t ${dockerRegisteryPrefix}/${containerName}:latest ."
              script.sh "docker push ${dockerRegisteryPrefix}/${containerName}:latest"
              script.sh "docker tag ${dockerRegisteryPrefix}/${containerName}:latest ${dockerRegisteryPrefix}/${containerName}:${containerTag}"
              script.sh "docker push ${dockerRegisteryPrefix}/${containerName}:${containerTag}"
//
          }


               //}
           //}
        }
            
    }     
@Override
    void deploy() {
        script.echo "Helm Deploy"
        def deployer =  new Deployer(script,"${script.env.JOB_NAME}",containerTag,helmRepoURL,helmRepo,helmGitRepo,helmGitRepoBranch,helmCrendetiaslId)
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

