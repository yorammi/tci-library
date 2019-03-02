#!/usr/bin/groovy

import groovy.time.*

def block(Map config, Closure body) {
    if (config == null) {
        config = [:]
    }

    if (config.name == null) {
        config.name = "TCI block"
    }
    if (config.failOnError == null) {
        config.failOnError = true
    }

    if (config.enable == null) {
        config.enable = true
    }

    if(!config.enable) {
        tciLogger.info(config.name+ " TCI block disabled.")
        return
    }

    tciLogger.info(config.name+ " TCI block started")
    def timeStart = new Date()
    try {
        body()
    }
    catch (error)
    {
        tciLogger.info ("[ERROR] "+error.message)
        if(config.failOnError) {
            tciLogger.info ("[ERROR] "+config.name+" TCI block failed!")
            throw error
        }
        else {
            tciLogger.info ("[WARNING] "+config.name+" TCI block failed, but block set to continue on error.")
        }
    }
    finally {
        def timeStop = new Date()
        def duration = TimeCategory.minus(timeStop, timeStart)
        tciLogger.info(config.name+ " TCI block ended. " +config.name+ " block duration: ${duration}")
    }
}

