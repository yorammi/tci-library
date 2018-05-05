def call(String requirementsFilePath) {
    File file = new File( env.WORKSPACE + "/" + requirementsFilePath )
    if( file.exists() && file.isFile()) {
        sh "pip install -r ${pythonRequirements}"
    }
}

