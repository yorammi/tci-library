def call(String requirementsFilePath) {
    File file = new File( script.env.WORKSPACE + "/" + requirementsFilePath )
    if( file.exists() && file.isFile()) {
        sh "pip install -r ${pythonRequirements}"
    }
}