#!/usr/bin/groovy

void info(String message) {
    tciGeneral.timedEcho("[INFO] "+message)
}

void debug(String message) {
    if (params.debugLogging || "${env.TCI_DEBUG_MODE}" == "true") {
        tciGeneral.timedEcho("[DEBUG] "+message)
    }
}
