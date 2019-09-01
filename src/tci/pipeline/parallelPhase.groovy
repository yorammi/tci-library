package tci.pipeline

import groovy.time.TimeCategory;

class parallelPhase implements Serializable {

    class subJob implements Serializable {

        String blockName
        def parameters
        boolean propagate
        boolean wait
        int retry
        String status
        String url
        def duration = null
        String title
        String alias

        subJob(String blockName, def parameters, boolean propagate, boolean wait, int retry, String alias ) {
            this.blockName = blockName
            this.parameters = parameters
            this.propagate = propagate
            this.wait = wait
            this.retry = retry
            this.alias = alias
        }
    }

    class stepsSequence implements Serializable {

        String blockName
        def sequence
        boolean propagate
        boolean wait
        int retry
        String status
        String url
        def duration = null
        String title
        String alias

        stepsSequence(String blockName, def sequence, boolean propagate, int retry, String alias ) {
            this.blockName = blockName
            this.sequence = sequence
            this.propagate = propagate
            this.retry = retry
            this.alias = alias
        }
    }

    def script
    def name = "TCI parallel"
    def jobs = []
    def stepsSequences = []
    boolean failFast = false
    boolean failOnError = true
    boolean showStages = true
    boolean showPhaseStage = false
    String overAllStatus = "SUCCESS"
    String description = ""

    parallelPhase(script, String name = "TCI parallel", boolean failFast = false, boolean failOnError = true, boolean showStages = true, boolean showPhaseStage = false) {
        script.echo "Deprecative constructor"
        this.script = script
        this.name = name
        this.failFast = failFast
        this.failOnError = failOnError
        this.showStages = showStages
        this.showPhaseStage = showPhaseStage
    }

    parallelPhase(script, Map config) {
        this.script = script
        if (config == null) {
            config = [:]
        }
        if (config.name != null) {
            this.name = config.name
        }
        if (config.failFast != null) {
            this.failFast = config.failFast
        }
        if (config.failOnError != null) {
            this.failOnError = config.failOnError
        }
        if (config.showStages != null) {
            this.showStages = config.showStages
        }
        if (config.showPhaseStage != null) {
            this.showPhaseStage = config.showPhaseStage
        }
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
        if (config.alias == null) {
            config.alias = ""
        }

        def job = new subJob(config.job, config.parameters, config.propagate, config.wait, config.retry, config.alias)
        jobs << job
    }

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
        if (config.alias == null) {
            config.alias = ""
        }

        def stepsSequence = new stepsSequence(config.name, config.sequence, config.propagate, config.retry, config.alias)
        stepsSequences << stepsSequence
    }

    @NonCPS
    def getBuildResult(def build) {
        try {
            return build.getResult()
        }
        catch (error) {
            script.echo "[ERROR] [getBuildResult] "+error.message
            return "N/A"
        }
    }

    @NonCPS
    def getBuildUrl(def build) {
        try {
            return build.getUrl()
        }
        catch (error) {
            script.echo "[ERROR] [getUrl] "+error.message
            return "N/A"
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

    def runJob(def item) {
        def timeStart = new Date()
        if(item.retry < 1) {
            item.retry = 1
        }
        def count=0
        try {
            while (count < item.retry) {
                try {
                    count++
                    def currentRun = script.build (job: item.blockName, parameters: item.parameters, propagate: false , wait: item.wait)
                    if(currentRun!=null) {
                        item.status = getBuildResult(currentRun)
                        item.url = getBuildUrl(currentRun)
                    }
                    if(item.status=="SUCCESS" || item.status=="ABORTED") {
                        count=item.retry
                    }
                    else {
                    }
                }
                catch (error) {
                    script.echo error.message
                    item.status = "FAILURE"
                }
            }
            setOverallStatusByItem(item)
        }
        catch (error) {
        }
        def timeStop = new Date()
        def duration = TimeCategory.minus(timeStop, timeStart)
        item.duration = duration.toString()
        def currentStatusColor=(item.status=="SUCCESS")?'\033[1;94m':'\033[1;91m'
        script.echo(" '\033[1;94m${item.title}\033[0m' (${item.url}) ended with ${currentStatusColor}${item.status}\033[0m status. Duration: \033[1;94m${duration}\033[0m")
        if(item.status!="SUCCESS") {
            throw new Exception()
        }
    }

    def runStepsSequence(def item) {
        def timeStart = new Date()
        if(item.retry < 1) {
            item.retry = 1
        }
        def count=0
        while (count < item.retry) {
            try {
                count++
                item.sequence()
                count=item.retry
            }
            catch (error) {
                script.echo error.message
                item.status = "FAILURE"
            }
        }
        setOverallStatusByItem(item)
        def timeStop = new Date()
        def duration = TimeCategory.minus(timeStop, timeStart)
        item.duration = duration.toString()
        script.tciLogger.info(" '\033[1;94m${item.title}\033[0m' ended with \033[1;94m${item.status}\033[0m status. Duration: \033[1;94m${duration}\033[0m")
    }

    void run() {
        if(showPhaseStage) {
            script.stage(name) {
                runImpl()
            }
        }
        else {
            runImpl()
        }
    }

    void runImpl() {
        def parallelBlocks = [:]

        def counter=1
        jobs.each { item ->
            def index = counter
            def title = "[Phase-job #"+counter+"] "+item.blockName
            if(item.alias != null && item.alias != "") {
                title = "[Phase-job #"+counter+"] "+item.alias
            }
            item.title = title
            item.status = "SUCCESS"
            item.url = ""
            parallelBlocks[title] = {
                if(showStages) {
                    script.stage(title) {
                        runJob(item)
                    }
                }
                else {
                    runJob(item)
                }
            }
            counter++
        }

        counter=1
        stepsSequences.each { item ->
            def index = counter
            def title = "[Phase-sequence #"+counter+"] "+item.blockName
            if(item.alias != null && item.alias != "") {
                title = "[Phase-sequence #"+counter+"] "+item.alias
            }
            item.title = title
            item.status = "SUCCESS"
            parallelBlocks[title] = {
                if(showStages) {
                    script.stage(title) {
                        runStepsSequence(item)
                    }
                }
                else {
                    runStepsSequence(item)
                }
            }
            counter++
        }

        parallelBlocks.failFast = failFast
        try {
            script.parallel parallelBlocks
        }
        catch (error) {
        }

        description = "\033[1;94m"+name+'\033[0m\n\n\033[1;92mRun in parallel:\033[0m\n'
        def currentFailuesDescription = "\033[1;91mFailed:\033[0m\n'"
        boolean failedSteps = false
        jobs.each { item ->
            def currentDescription = ""
            def currentStatusColor=(item.status=="SUCCESS")?'\033[1m':'\033[1;91m'
            def currentStatus = currentStatusColor+item.status+'\033[0m'
            if(item.propagate == false) {
                currentStatus += " (propagate:false)"
            }
            currentDescription += '\t'+item.title+' - '+currentStatus+' - '+item.url
            if(item.duration!=null) {
                currentDescription += ' - '+item.duration
            }
            currentDescription += '\n'
            description+=currentDescription
            if(item.status!="SUCCESS") {
                failedSteps = true
                currentFailuesDescription+=currentDescription
            }
        }
        stepsSequences.each { item ->
            def currentDescription = ""
            def currentStatusColor=(item.status=="SUCCESS")?'\033[1m':'\033[1;91m'
            def currentStatus = currentStatusColor+item.status+'\033[0m'
            if(item.propagate == false) {
                currentStatus += " (propagate:false)"
            }
            currentDescription += '\t'+item.title+' - '+currentStatus
            if(item.duration!=null) {
                currentDescription += ' - '+item.duration
            }
            currentDescription += '\n'
            description+=currentDescription
            if(item.status!="SUCCESS") {
                failedSteps = true
                currentFailuesDescription+=currentDescription
            }
        }
        String statusColor="\033[1;92m"
        if(overAllStatus=="FAILURE") {
            statusColor="\033[1;91m"
        }
        else {
            if(overAllStatus=="UNSTABLE") {
                statusColor="\033[0;103m"
            }
            else {
                if(overAllStatus=="ABORTED") {
                    statusColor="\033[1;90m"
                }
                else {

                }
            }
        }
        description += "\n'\033[1;94m"+name+"\033[0m' parallel phase status: "+statusColor+overAllStatus+"\033[0m\n"
        if(failedSteps) {
            description += "\n"+currentFailuesDescription
        }
        script.echo description
        if(failOnError) {
            script.currentBuild.result = overAllStatus
            if(overAllStatus=="FAILURE"|| overAllStatus=="ABORTED") {
                script.error ("\n'\033[1;94m"+name+"\033[0m' parallel phase status: "+statusColor+overAllStatus+"\033[0m\n")
            }
        }
    }
}

