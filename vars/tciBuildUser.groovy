#!/usr/bin/groovy

def initBuildUserEnvVars() {
    wrap([$class: 'BuildUser']) {
        echo "1"
        env.BUILD_USER = BUILD_USER
        echo "2"
        env.BUILD_USER_FIRST_NAME = BUILD_USER_FIRST_NAME
        echo "3"
        env.BUILD_USER_LAST_NAME = BUILD_USER_LAST_NAME
        echo "4"
        env.BUILD_USER_ID = BUILD_USER_ID
        echo "5"
        env.BUILD_USER_EMAIL = BUILD_USER_EMAIL
        echo "6"
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
