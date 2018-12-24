#!/usr/bin/groovy

def call(String text) {
    tciGeneral.deprecatedMessage("tplEcho","tciGeneral.timedEcho")
    tciGeneral.timedEcho(text)
}
