def call(String findText) {

    String logText = currentBuild.rawBuild.getLog()

    if(logText.contains(findText))
    {
        println("Found ${findText} in build log")
        return true
    }
    return false
}