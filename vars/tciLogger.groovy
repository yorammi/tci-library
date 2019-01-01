#!/usr/bin/groovy

void info(String message) {
    tciGeneral.timedEcho(message)
}

void debug(String message) {
    def debugFlag = script.env.TCI_DEBUG_MODE
    if (params.debugLogging || "${debugFlag}" == "true") {
        tciGeneral.timedEcho(message)
    }
}
