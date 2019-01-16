#!/usr/bin/groovy

def initDefaults() {
    if("${env.TCI_DEBUG_MODE}" != "true" && "${env.TCI_DEBUG_MODE}" != "false") {
        env.TCI_DEBUG_MODE="false"
        echo "env.TCI_DEBUG_MODE=${env.TCI_DEBUG_MODE}"
    }
}

