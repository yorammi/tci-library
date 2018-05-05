def call(String scriptFilePath) {
    File file = new File( script.env.WORKSPACE + "/" + scriptFilePath )
    if( file.exists() && file.isFile()) {
        sh "python ./${scriptFilePath}"
    }
}