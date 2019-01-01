package tpl.utils;

class Logger implements Serializable {
    def script
    boolean debugLogging = false

    Logger(script) {
        this.script = script
        debugLogging = (script.params.debugLogging || script.env.TCI_DEBUG_MODE)  ?: false
    }

    void info(String message) {
        script.tciGeneral.timedEcho(message)
    }

    void debug(String message) {
        if (debugLogging) {
            script.tciGeneral.timedEcho(message)
        }
    }


}