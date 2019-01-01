#!/usr/bin/groovy

void info(String message) {
    tciGeneral.timedEcho(message)
}

void debug(String message) {
    if (params.debugLogging || params.TCI_DEBUG_MODE || env.TCI_DEBUG_MODE) {
        tciGeneral.timedEcho(message)
    }
}
