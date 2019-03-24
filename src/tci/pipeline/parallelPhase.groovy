package tci.pipeline

import groovy.time.TimeCategory;

class parallelPhase implements Serializable {

    class subJob implements Serializable {

        String jobName
        def parameters
        boolean propagate
        boolean wait
        int retry
        String status
        String url
        def duration
        String title

        subJob(String jobName, def parameters, boolean propagate, boolean wait, int retry ) {
            this.jobName = jobName
            this.parameters = parameters
            this.propagate = propagate
            this.wait = wait
            this.retry = retry
        }
    }

//    class subRemoteJob implements Serializable {
//
//        String jobName
//        String remoteJenkinsName
//        def parameters
//        boolean abortTriggeredJob
//        boolean useCrumbCache
//        boolean useJobInfoCache
//        int pollInterval
//        int retry
//        String status
//        String url
//        def duration
//        String title
//
//        subRemoteJob(String jobName, String remoteJenkinsName, def parameters, boolean abortTriggeredJob, boolean useCrumbCache, boolean useJobInfoCache, int pollInterval, int retry ) {
//            this.jobName = jobName
//            this.remoteJenkinsName = remoteJenkinsName
//            this.parameters = parameters
//            this.abortTriggeredJob = abortTriggeredJob
//            this.useCrumbCache = useCrumbCache
//            this.useJobInfoCache = useJobInfoCache
//            this.pollInterval = pollInterval
//            this.retry = retry
//        }
//    }

    class stepsSequence implements Serializable {

        String sequenceName
        def sequence
        boolean propagate
        boolean wait
        int retry
        String status
        String url
        def duration
        String title

        stepsSequence(String sequenceName, def sequence, boolean propagate, int retry ) {
            this.sequenceName = sequenceName
            this.sequence = sequence
            this.propagate = propagate
            this.retry = retry
        }
    }

    def script
    def name
    def jobs = []
//    def remoteJobs = []
    def stepsSequences = []
    boolean failFast = false
    boolean failOnError = false
    String overAllStatus = "SUCCESS"
    String description = ""

    parallelPhase(script, String name = "TCI parallel", boolean failFast = false, boolean failOnError = false) {
        this.script = script
        this.name = name
        this.failFast = failFast
        this.failOnError = failOnError
    }

    void addSubJob(Map config) {
        if (config == null) {
            config = [:]
        }
        if (config.job == null) {
            script.tciLogger.info ("[ERROR] you must provive a job name to run!!!")
            throw Exception
        }
        if (config.propagate == null) {
            config.propagate = true
        }
        if (config.parameters == null) {
            config.parameters = null
        }
        if (config.wait == null) {
            config.wait = true
        }
        if (config.retry == null) {
            config.retry = 1
        }

        def job = new subJob(config.job, config.parameters, config.propagate, config.wait, config.retry)
        jobs << job
    }

//    void addRemoteSubJob(Map config) {
//        if (config == null) {
//            config = [:]
//        }
//        if (config.job == null) {
//            script.tciLogger.info ("[ERROR] you must provive a job name to run!!!")
//            throw Exception
//        }
//        if (config.remoteJenkinsName == null) {
//            script.tciLogger.info ("[ERROR] you must provive the remote Jenkins server name (remoteJenkinsName) name to run!!!")
//            throw Exception
//        }
//        if (config.abortTriggeredJob == null) {
//            config.abortTriggeredJob = true
//        }
//        if (config.useCrumbCache == null) {
//            config.useCrumbCache = true
//        }
//        if (config.useJobInfoCache == null) {
//            config.useJobInfoCache = true
//        }
//        if (config.parameters == null) {
//            config.parameters = null
//        }
//        if (config.wait == null) {
//            config.wait = true
//        }
//        if (config.pollInterval == null) {
//            config.pollInterval = 30
//        }
//        if (config.retry == null) {
//            config.retry = 1
//        }
//
//        def remoteJob = new subRemoteJob(config.job, config.remoteJenkinsName, config.parameters, config.abortTriggeredJob, config.useCrumbCache, config.useJobInfoCache, config.pollInterval, config.retry)
//        remoteJobs << remoteJob
//    }

    void addStepsSequence(Map config) {
        if (config == null) {
            config = [:]
        }
        if (config.sequence == null) {
            script.tciLogger.info ("[ERROR] you must provive a block of steps sequence to run!!!")
            throw Exception
        }
        if (config.name == null) {
            config.name = "Steps sequence"
        }
        if (config.propagate == null) {
            config.propagate = true
        }
        if (config.retry == null) {
            config.retry = 1
        }

        def stepsSequence = new stepsSequence(config.name, config.sequence, config.propagate, config.retry)
        stepsSequences << stepsSequence
    }

    @NonCPS
    def getBuildResult(def build) {
        try {
            return build.getResult()
        }
        catch (error) {
            script.echo "[ERROR] [getBuildResult] "+error.message
        }
    }

    @NonCPS
    def getBuildUrl(def build) {
        try {
            return build.getAbsoluteUrl()
        }
        catch (error) {
            script.echo "[ERROR] [getBuildUrl] "+error.message
        }
    }

    def runJob(def item) {
        if(item.retry < 1) {
            item.retry = 1
        }
        def count=0
        while (count < item.retry) {
            try {
                count++
                def currentRun = script.build (job: item.jobName, parameters: item.parameters, propagate: false , wait: item.wait)
                if(currentRun!=null) {
                    item.status = getBuildResult(currentRun)
                    item.url = getBuildUrl(currentRun)
                }
                if(item.status=="SUCCESS" || item.status=="ABORTED") {
                    count=item.retry
                }
            }
            catch (error) {
                script.echo error.message
                item.status = "FAILURE"
            }
        }
    }

    def setOverallStatusByItem(def item) {
        if(item.propagate == true) {
            if(item.status == "FAILURE") {
                overAllStatus="FAILURE"
            }
            else {
                if(item.status == "UNSTABLE") {
                    if(item.overAllStatus != "FAILURE") {
                        overAllStatus="UNSTABLE"
                    }
                }
                else {
                    if(item.status == "ABORTED") {
                        if(item.overAllStatus != "FAILURE" && item.overAllStatus != "UNSTABLE") {
                            overAllStatus="ABORTED"
                        }
                    }
                }
            }
        }
    }

    void run() {
        def parallelBlocks = [:]

        def counter=1
        jobs.each { item ->
            def index = counter
            def title = "[Job #"+counter+"] "+item.jobName
            item.title = title
            item.status = "SUCCESS"
            item.url = ""
            parallelBlocks[title] = {
                script.stage(title) {
                    def timeStart = new Date()
                    runJob(item)
                    def timeStop = new Date()
                    def duration = TimeCategory.minus(timeStop, timeStart)
                    script.tciLogger.info(" Parallel job '\033[1;94m${item.jobName}\033[0m' ended. Duration: \033[1;94m${duration}\033[0m")
                    setOverallStatusByItem(item)
//                    if(item.propagate == true) {
//                        if(item.status == "FAILURE") {
//                            overAllStatus="FAILURE"
//                        }
//                        else {
//                            if(item.status == "UNSTABLE") {
//                                if(item.overAllStatus != "FAILURE") {
//                                    overAllStatus="UNSTABLE"
//                                }
//                            }
//                            else {
//                                if(item.status == "ABORTED") {
//                                    if(item.overAllStatus != "FAILURE" && item.overAllStatus != "UNSTABLE") {
//                                        overAllStatus="ABORTED"
//                                    }
//                                }
//                            }
//                        }
//                    }
                }
            }
            counter++
        }

//        counter=1
//        remoteJobs.each { item ->
//            def index = counter
//            def title = "[Remote job #"+counter+"] "+item.jobName
//            item.title = title
//            parallelBlocks[title] = {
//                script.stage(title) {
//                    def timeStart = new Date()
//                    if( item.retry > 1) {
//                        script.retry (item.retry) {
//                            script.triggerRemoteJob (remoteJenkinsName: item.remoteJenkinsName, job: item.jobName, parameters: item.parameters, abortTriggeredJob: item.propagate, pollInterval: item.pollInterval, useCrumbCache: temm.useCrumbCache, useJobInfoCache: item.useJobInfoCache ,maxConn: 1)
//                        }
//                    }
//                    else {
//                        script.triggerRemoteJob (remoteJenkinsName: item.remoteJenkinsName, job: item.jobName, parameters: item.parameters, abortTriggeredJob: item.propagate, pollInterval: item.pollInterval, useCrumbCache: temm.useCrumbCache, useJobInfoCache: item.useJobInfoCache ,maxConn: 1)
//                    }
//                    def timeStop = new Date()
//                    def duration = TimeCategory.minus(timeStop, timeStart)
//                    script.tciLogger.info(" Parallel remote job '${item.jobName}' ended. Duration: ${duration}")
//                }
//            }
//            counter++
//        }

        counter=1
        stepsSequences.each { item ->
            def index = counter
            def title = "[Sequence #"+counter+"] "+item.sequenceName
            item.title = title
            parallelBlocks[title] = {
                script.stage(title) {
                    def timeStart = new Date()
                    item.status = "SUCCESS"
                    try {
                        if( item.retry > 1) {
                            script.retry (item.retry) {
                                item.sequence()
                            }
                        }
                        else {
                            item.sequence()
                        }
                    }
                    catch (error) {
                        item.status = "FAILURE"
                    }
                    if(item.propagate == true) {
                        if (item.status == "FAILURE") {
                            overAllStatus = "FAILURE"
                        } else {
                            if (item.status == "UNSTABLE") {
                                if (item.overAllStatus != "FAILURE") {
                                    overAllStatus = "UNSTABLE"
                                }
                            } else {
                                if (item.status == "ABORTED") {
                                    if (item.overAllStatus != "FAILURE" && item.overAllStatus != "UNSTABLE") {
                                        overAllStatus = "ABORTED"
                                    }
                                }
                            }
                        }
                    }
                    def timeStop = new Date()
                    def duration = TimeCategory.minus(timeStop, timeStart)
                    script.tciLogger.info(" Parallel steps-sequence '\033[1;94m${item.sequenceName}\033[0m' ended. Duration: \033[1;94m${duration}\033[0m")
                }
            }
            counter++
        }

        script.tciPipeline.block (name:name,failOnError:failOnError) {
            parallelBlocks.failFast = failFast
            try {
                script.parallel parallelBlocks
            }
            catch (error) {
            }

            description = "\033[1;94m"+name+'\033[0m\n'
            jobs.each { item ->
                def currentStatus = item.status
                if(item.propagate == false) {
                    currentStatus += " (propagate:false)"
                }
                description += '\t'+item.title+' - '+currentStatus+' - '+item.url+'\n'
            }
//            remoteJobs.each { item ->
//                description += '\t'+item.title+'\n'
//            }
            stepsSequences.each { item ->
                def currentStatus = item.status
                if(item.propagate == false) {
                    currentStatus += " (propagate:false)"
                }
                description += '\t'+item.title+' - '+currentStatus+'\n'
            }
            String statusColor="\033[1;92m"
            if(overAllStatus=="FAILURE") {
                statusColor="\033[1;91m"
            }
            else {
                if(overAllStatus=="UNSTABLE") {
                    statusColor="\033[1;93m"
                }
                else {
                    if(overAllStatus=="ABORTED") {
                        statusColor="\033[1;90m"
                    }
                    else {

                    }
                }
            }
            description += "'"+name+"' phase status: "+statusColor+overAllStatus+'\033[0m\n'
            script.echo description
            script.currentBuild.result = overAllStatus
            if (overAllStatus=="FAILURE" || overAllStatus=="ABORTED") {
                script.error "\033[1;91m[ERROR]\033[0m phase '${name}' Failed"
            }
        }
    }
}

