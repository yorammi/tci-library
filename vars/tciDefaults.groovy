#!/usr/bin/groovy

def initDefaults() {
    if("${env.TCI_DEBUG_MODE}" != "true" && "${env.TCI_DEBUG_MODE}" != "false") {
        env.TCI_DEBUG_MODE="false"
    }
}

def isDebugMode() {
    if("${env.TCI_DEBUG_MODE}" != "true" && "${env.TCI_DEBUG_MODE}" != "false") {
        echo "1"
        initDefaults()
    }
    if("${env.TCI_DEBUG_MODE}" == "true") {
        echo "2"
        return true
    }
    echo "3"
    return false
}
