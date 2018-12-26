#!/usr/bin/groovy

def initBuildUserEnvVars() {
    wrap([$class: 'BuildUser']) {
        env.BUILD_USER = BUILD_USER
        env.BUILD_USER_FIRST_NAME = BUILD_USER_FIRST_NAME
//        env.BUILD_USER_LAST_NAME = BUILD_USER_LAST_NAME
        env.BUILD_USER_LAST_NAME = BUILD_USER_FIRST_NAME
        env.BUILD_USER_ID = BUILD_USER_ID
        env.BUILD_USER_EMAIL = BUILD_USER_ID
//        env.BUILD_USER_EMAIL = BUILD_USER_EMAIL
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
