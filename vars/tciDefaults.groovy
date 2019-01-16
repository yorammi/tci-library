#!/usr/bin/groovy

def initDefaults() {
    if("${env.TCI_DEBUG_MODE}" != "true" && "${env.TCI_DEBUG_MODE}" != "false") {
        env.TCI_DEBUG_MODE="false"
    }
}

def isDebugMode() {
    if("${env.TCI_DEBUG_MODE}" != "true" && "${env.TCI_DEBUG_MODE}" != "false") {
        initDefaults()
    }
    if("${env.TCI_DEBUG_MODE}" == "true") {
        return true
    }
    return false
}
