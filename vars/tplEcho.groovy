#!/usr/bin/groovy

def call(String text) {
    tciGeneral.deprecatedMessage("echo","tciGeneral.timedEcho")
    tciGeneral.timedEcho(text)
}
