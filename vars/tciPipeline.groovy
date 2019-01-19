#!/usr/bin/groovy

import groovy.time.*

def phase(String phaseName, Closure body) {
//def phase(String phaseName, Map config, Closure body) {
//    if (config == null) {
//        config = [:]
//    }
//
//    if (config.failOnError == null) {
//        config.failOnError = true
//    }
//
//    if (config.enable == null) {
//        config.enable = true
//    }
//
//    if(!config.enable) {
//        tciLogger.info(phaseName+ " TCI phase disabled.")
//        return
//    }

    tciLogger.info(phaseName+ " TCI phase started")
    def timeStart = new Date()
    try {
        body()
    }
    catch (error)
    {
        if(config.failOnError) {
            script.tciLogger.info ("[ERROR] TCI phase failed!")
            throw error
        }
        else {
            script.tciLogger.info ("[WARNING] TCI phase failed, but phase set to continue on error.")
        }
    }
    finally {
        def timeStop = new Date()
        def duration = TimeCategory.minus(timeStop, timeStart)
        tciLogger.info(phaseName+ " TCI phase ended." +phaseName+ " phase duration: ${duration}")
    }
}





