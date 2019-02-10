#!/usr/bin/groovy
// Build user functions

def initBuildUserEnvVars() {
    wrap([$class: 'BuildUser']) {
        try {
            env.BUILD_USER = BUILD_USER
        }
        catch (Error) {
            env.BUILD_USER = ""
        }
        try {
            env.BUILD_USER_FIRST_NAME = BUILD_USER_FIRST_NAME
        }
        catch (Error) {
            env.BUILD_USER_FIRST_NAME = ""
        }
        try {
            env.BUILD_USER_LAST_NAME = BUILD_USER_LAST_NAME
        }
        catch (Error) {
            env.BUILD_USER_LAST_NAME = ""
        }
        try {
            env.BUILD_USER_ID = BUILD_USER_ID
        }
        catch (Error) {
            env.BUILD_USER_ID = ""
        }
        try {
            env.BUILD_USER_EMAIL = BUILD_USER_EMAIL
        }
        catch (Error) {
            env.BUILD_USER_EMAIL = ""
        }
    }
}

def getBuildUserDisplayName() {
    if(!env.BUILD_USER) {
        initBuildUserEnvVars()
    }
    return env.BUILD_USER
}

def getBuildUserFirstName() {
    if(!env.BUILD_USER_FIRST_NAME) {
        initBuildUserEnvVars()
    }
    return env.BUILD_USER_FIRST_NAME
}

def getBuildUserLastName() {
    if(!env.BUILD_USER_LAST_NAME) {
        initBuildUserEnvVars()
    }
    return env.BUILD_USER_LAST_NAME
}

def getBuildUserID() {
    if(!env.BUILD_USER_ID) {
        initBuildUserEnvVars()
    }
    return env.BUILD_USER_ID
}

def getBuildUserEmail() {
    if(!env.BUILD_USER_EMAIL) {
        initBuildUserEnvVars()
    }
    return env.BUILD_USER_EMAIL
}

def buildStartedByUser() {
    try{
        def isStartedByUser = currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause) != null
        return isStartedByUser
    }
    catch (Exception error)
    {
        return false
    }
}
