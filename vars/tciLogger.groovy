#!/usr/bin/groovy

void info(String message) {
    tciGeneral.timedEcho(message)
}

void debug(String message) {
    if (params.debugLogging || "${script.env.TCI_DEBUG_MODE}" == "true") {
        tciGeneral.timedEcho(message)
    }
}
