#!/usr/bin/groovy

void info(String message) {
    try {
        tciGeneral.timedEcho("[INFO] "+message)
    }
    catch (error) {

    }
}

void debug(String message) {
    try {
        if (params.debugLogging || tciDefaults.isDebugMode) {
            tciGeneral.timedEcho("[DEBUG] "+message)
        }
    }
    catch (error) {

    }
}
