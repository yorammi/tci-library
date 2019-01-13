#!/usr/bin/env groovy

def mavenCmd(Map config) {
    if (config == null) {
        config = [:]
    }

    if (config.jdk == null) {
        config.jdk = 'jdk'
    }

    if (config.settings == null) {
        config.settings = 'maven-settings'
    }

    if (config.mavenVersion == null) {
        config.mavenVersion = 'maven'
    }

    if (config.dir == null) {
        config.dir = '.'
    }

    if (config.cmd == null) {
        config.cmd = 'clean install'
    }

    if (config.additionalCmd == null) {
        config.additionalCmd = ''
    }

    def mavenCommand = "${config.cmd} ${config.additionalCmd}"

    withMaven(globalMavenSettingsConfig: config.settings, jdk: config.jdk, maven: config.mavenVersion) {
        dir(config.dir) {
            sh "mvn ${mavenCommand}"
        }
    }

}

void mavenCompile(Map config) {
    if (config == null) {
        config = [:]
    }

    if (config.jdk == null) {
        config.jdk = 'jdk'
    }

    if (config.mavenVersion == null) {
        config.mavenVersion = 'maven'
    }

    if (config.dir == null) {
        config.dir = '.'
    }

    if (config.additionalCmd == null) {
        config.additionalCmd = ''
    }

    def mavenCommand = "${config.cmd} ${config.additionalCmd}"

    mavenCmd(jdk:config.jdk, mavenVersion:config.mavenVersion, dir:config.dir ,cmd:"clean compile -Dmaven.test.skip=true"+config.additionalCmd)
}

void mavenRunUnitTests(Map config) {
    if (config == null) {
        config = [:]
    }

    if (config.jdk == null) {
        config.jdk = 'jdk'
    }

    if (config.mavenVersion == null) {
        config.mavenVersion = 'maven'
    }

    if (config.dir == null) {
        config.dir = '.'
    }

    if (config.additionalCmd == null) {
        config.additionalCmd = ''
    }

    def mavenCommand = "test -Dmaven.test.failure.ignore=true ${config.additionalCmd}"

    mavenCmd(jdk:config.jdk, mavenVersion:config.mavenVersion, dir:config.dir ,cmd:mavenCommand)
}

void mavenPackage(Map config) {
    if (config == null) {
        config = [:]
    }

    if (config.jdk == null) {
        config.jdk = 'jdk'
    }

    if (config.mavenVersion == null) {
        config.mavenVersion = 'maven'
    }

    if (config.dir == null) {
        config.dir = '.'
    }

    if (config.additionalCmd == null) {
        config.additionalCmd = ''
    }

    def mavenCommand = "package -Dmaven.test.skip=true ${config.additionalCmd}"

    mavenCmd(jdk:config.jdk, mavenVersion:config.mavenVersion, dir:config.dir ,cmd:mavenCommand)
}

void mavenDeploy(Map config) {
    if (config == null) {
        config = [:]
    }

    if (config.jdk == null) {
        config.jdk = 'jdk'
    }

    if (config.mavenVersion == null) {
        config.mavenVersion = 'maven'
    }

    if (config.dir == null) {
        config.dir = '.'
    }

    if (config.additionalCmd == null) {
        config.additionalCmd = ''
    }

    def mavenCommand = "deploy -Dmaven.test.skip=true ${config.additionalCmd}"

    mavenCmd(jdk:config.jdk, mavenVersion:config.mavenVersion, dir:config.dir ,cmd:mavenCommand)
}

void setVersion(Map config) {
    if (config == null) {
        config = [:]
    }

    if (config.version == null) {
        config.version = '0.0.0.0'
    }

    if (config.jdk == null) {
        config.jdk = 'jdk'
    }

    if (config.mavenVersion == null) {
        config.mavenVersion = 'maven'
    }

    if (config.dir == null) {
        config.dir = '.'
    }

    if (config.additionalCmd == null) {
        config.additionalCmd = ''
    }

    def mavenCommand = "versions:set -DnewVersion=${config.version} -DgenerateBackupPoms=false ${config.additionalCmd}"

    mavenCmd(jdk:config.jdk, mavenVersion:config.mavenVersion, dir:config.dir ,cmd:mavenCommand)
}

