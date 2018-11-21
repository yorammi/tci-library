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
    def helmCrendetialId
    def awsCrendetialId
    def kubeContext
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
    helmCrendetialId = (script.params.get('helmCrendetialId') == null ) ? 'Missing Helm-Jenkins Credentials id' : script.params.get('helmCrendetialId')
    awsCrendetialId = (script.params.get('awsCrendetialId') == null ) ? 'Missing AWS-Jenkins Credentials id' : script.params.get('awsCrendetialId')
    kubeContext = (script.params.get('kubeContext') == null ) ? 'Missing kubeContext' : script.params.get('kubeContext')
  }

@Override
    void setup() {
        gitConfig()
        initParams()


        // automatically capture environment variables while downloading and uploading files
    }

    
@Override
    void checkout() {
    script.echo "Before checktout"
//        script.checkout script.scm
    script.echo "After checktout"
    }
@Override
    void deploy() {
        script.echo "Helm Deploy"
        def deployer =  new Deployer(script,"${script.env.JOB_NAME}",containerTag,helmRepoURL,helmRepo,helmGitRepo,helmGitRepoBranch,helmCrendetialId,awsCrendetialId,kubeContext)
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

