#!/usr/bin/groovy

import groovy.time.*

def phase(Map config, Closure body) {
    if (config == null) {
        config = [:]
    }

    if (config.name == null) {
        config.name = "TCI phase"
    }
    if (config.failOnError == null) {
        config.failOnError = true
    }

    if (config.enable == null) {
        config.enable = true
    }

    if(!config.enable) {
        tciLogger.info(config.name+ " TCI phase disabled.")
        return
    }

    tciLogger.info(config.name+ " TCI phase started")
    def timeStart = new Date()
    try {
        body()
    }
    catch (error)
    {
        if(config.failOnError) {
            script.tciLogger.info ("[ERROR] "+config.name+" TCI phase failed!")
            throw error
        }
        else {
            script.tciLogger.info ("[WARNING] "+config.name+" TCI phase failed, but phase set to continue on error.")
        }
    }
    finally {
        def timeStop = new Date()
        def duration = TimeCategory.minus(timeStop, timeStart)
        tciLogger.info(config.name+ " TCI phase ended." +config.name+ " phase duration: ${duration}")
    }
}

