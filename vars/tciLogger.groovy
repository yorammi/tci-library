#!/usr/bin/groovy

void info(String message) {
    script.echo message
}

void debug(String message) {
    if (debugLogging) {
        script.echo message
    }
}
