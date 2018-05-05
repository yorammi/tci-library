package tpl.python

import com.cloudbees.groovy.cps.NonCPS

abstract class tplBaseGenericPythonRunner implements Serializable {

    def script
    def gitBranch
    def gitRepoUrl
    def gitCredentialsId
    def pythonRequirements
    def pythonScript
    def groovySetupScript

    tplBaseGenericPythonRunner(script) {
        this.script = script
    }

    void run() {
        script.timestamps() {
            runImpl()
        }
    }

    void runStage(String name, Closure stage) {
        script.echo "--- Start stage [$name] ---"
        script.stage(name, stage)
        script.echo "--- End stage [$name] ---"
    }

    void runImpl() {
        runStage('Setup', this.&setup)
        runStage('Checkout', this.&checkout)
        runStage('Requirements', this.&requirements)
        runStage('Script Run', this.&scriptRun)
    }

    void setup() {
        initParams()
        populateBuildInfo()
    }

    void initParams() {
        gitCredentialsId = script.params.gitCredentialsId
        gitRepoUrl = script.params.gitRepoUrl
        pythonRequirements = script.params.pythonRequirements
        pythonScript = script.params.pythonScript
        groovySetupScript = script.params.groovySetupScript
    }

    void populateBuildInfo() {
        populateBuildDisplayName()
        populateBuildDescription()
    }

    void populateBuildDisplayName() {
        def userId = script.tplGetBuildUserId()
        def scriptName = pythonScript.tokenize('/')[-1]
        script.currentBuild.displayName = "${script.currentBuild.displayName} | ${userId} | ${scriptName}"
    }

    void populateBuildDescription() {
        String description = ""
        script.currentBuild.description = description

    }

    void checkout() {
        script.git credentialsId: gitCredentialsId, url: gitRepoUrl, branch: gitBranch

    }

    void requirements(){
        commonRequirements()
        additionalGroovySetup()
        script.echo "Install Python requirements file '${pythonRequirements}'"
        def file = new File( script.env.WORKSPACE + "/" + pythonRequirements )
        if( file.exists() && file.isFile()) {
            script.sh "pip install -r ${pythonRequirements}"
        }
    }

    void commonRequirements(){
    }

    void additionalGroovySetup(){
        try
        {
            if( groovySetupScript?.trim() )
            {
                script.writeFile encoding: 'UTF-8',file: './variables.groovy', text: groovySetupScript
                script.load './variables.groovy'
                new File('./variables.groovy').delete()
            }
        }
        catch (error)
        {
        }
    }

    void scriptRun(){
        script.echo "Run Python script '${pythonScript}'"
        script.sh "python ./${pythonScript}"
    }
}
