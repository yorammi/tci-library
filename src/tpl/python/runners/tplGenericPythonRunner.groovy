package tpl.python.runners;

import tpl.python.tplBaseGenericPythonRunner

class tplGenericPythonRunner extends tplBaseGenericPythonRunner {

    tplGenericPythonRunner(script) {
        super(script)
    }


    @Override
    void initParams()
    {
        gitCredentialsId = script.env.GIT_TOKEN
        gitRepoUrl = script.env.REPOSITORY
        gitBranch = script.env.BRANCH
        pythonRequirements = script.env.REQUIREMENTS_FILE
        pythonScript = script.env.PYTHON_SCRIPT_FILE
        groovySetupScript = script.params.GROOVY_SCRIPT
    }

    @Override
    void commonRequirements(){
        script.echo "Install common requirements"
        script.sh '''
            pip install requests
        '''
    }

}
