#!/usr/bin/groovy

def initDefaults() {
    if("${env.TCI_DEBUG_MODE}" != "true" && "${env.TCI_DEBUG_MODE}" != "false") {
        env.TCI_DEBUG_MODE="false"
    }
    if (!env.TCI_DEFAULT_GIT_CREDENTIAL) {
        env.TCI_DEFAULT_GIT_CREDENTIAL="gitsshkey"
    }
    if (!env.TCI_DEFAULT_JDK_TOOL_NAME) {
        env.TCI_DEFAULT_JDK_TOOL_NAME="jdk"
    }
    if (!env.TCI_DEFAULT_MAVEN_TOOL_NAME) {
        env.TCI_DEFAULT_MAVEN_TOOL_NAME="maven"
    }
    if (!env.TCI_DEFAULT_MAVEN_SETTINGS_FILE_CONFIG_NAME) {
        env.TCI_DEFAULT_MAVEN_SETTINGS_FILE_CONFIG_NAME="maven-settings"
    }
}

def isDebugMode() {
    if("${env.TCI_DEBUG_MODE}" != "true" && "${env.TCI_DEBUG_MODE}" != "false") {
        initDefaults()
    }
    if("${env.TCI_DEBUG_MODE}" == "true") {
        return true
    }
    return false
}

def defaultGitCredential() {
    if (!env.TCI_DEFAULT_GIT_CREDENTIAL) {
        initDefaults()
    }
    return env.TCI_DEFAULT_GIT_CREDENTIAL
}

def defaultJdkTool() {
    if (!env.TCI_DEFAULT_JDK_TOOL_NAME) {
        initDefaults()
    }
    return env.TCI_DEFAULT_JDK_TOOL_NAME
}

def defaultMavenTool() {
    if (!env.TCI_DEFAULT_MAVEN_TOOL_NAME) {
        initDefaults()
    }
    return env.TCI_DEFAULT_MAVEN_TOOL_NAME
}

def defaultMavenSetting() {
    if (!env.TCI_DEFAULT_MAVEN_TOOL_NAME) {
        initDefaults()
    }
    return env.TCI_DEFAULT_MAVEN_TOOL_NAME
}

