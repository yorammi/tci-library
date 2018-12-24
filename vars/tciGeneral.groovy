#!/usr/bin/groovy

@NonCPS
def timedEcho(String text) {

    def out
    def config = new HashMap()
    def bindings = getBinding()
    config.putAll(bindings.getVariables())
    out = config['out']

    def now = new Date()
    def tstamp = now.format("yyyyMMdd-HH:mm:ss.SSS", TimeZone.getTimeZone('UTC'))

    echo "["+tstamp+"] " + text;
}

def deprecatedMessage(String oldStepName, String newStepName) {
    sh "echo -e \"\\e[31mThe step "+stepName+" is deprecated. please use "+newStepName+" instead\\e[0m\""
}
