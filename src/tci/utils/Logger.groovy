package tpl.utils;

class Logger implements Serializable {
    def script
    boolean debugLogging = false

    Logger(script) {
        this.script = script
        debugLogging = (script.params.debugLogging || script.params.TCI_DEBUG_MODE || script.env.TCI_DEBUG_MODE)  ?: false
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