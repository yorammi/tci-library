def call(String findText) {
    tciGeneral.deprecatedMessage("tplEcho","tciGeneral.findStringInBuildLog")
    return tciGeneral.findStringInBuildLog(findText)
}