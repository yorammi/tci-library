def call(String scriptFilePath) {
    File file = new File( env.WORKSPACE + "/" + scriptFilePath )
    if( file.exists() && file.isFile()) {
        sh "python ./${scriptFilePath}"
    }
}

