def call(String searchText) {

    String logText = currentBuild.rawBuild.getLog()
    if(logText.contains(searchText))
    {
        println("Found '${searchText}' in build log")
        currentBuild.result = 'FAILURE'
    }
}