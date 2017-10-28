package com.tikalk.utils;

class Logger implements Serializable {
    def script
    boolean debugLogging = false

    Logger(script) {
        this.script = script
        debugLogging = script.params.debugLogging ?: false
    }

    void info(String message) {
        script.echo message
    }

    void debug(String message) {
        if (debugLogging) {
            script.echo message
        }
    }


}