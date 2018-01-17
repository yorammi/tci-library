def call(Map config) {

    try
    {
        if (config == null)
        {
            config = [:]
        }
        if (config.additionalMessageText == null)
        {
            config.additionalMessageText = ''
        }
        if (config.notifyOnSuccess == null)
        {
            config.notifyOnSuccess = false
        }

        def previousBuildResult = null
        try
        {
            previousBuildResult=currentBuild.rawBuild.getPreviousBuild()?.getResult()
        }
        catch (error)
        {

        }
        if(config.buildStatus == null || config.buildStatus == "")
        {
            config.buildStatus = currentBuild.currentResult
        }
        string prevBuildMessage = ""
        if("${config.buildStatus}" == "SUCCESS" && "${previousBuildResult}" == "SUCCESS" && !config.notifyOnSuccess)
        {
            return
        }
        if("${config.buildStatus}" != "${previousBuildResult}" && "${config.buildStatus}" != "START" && "${config.buildStatus}" != "DONE")
        {
            prevBuildMessage = "(previous build was: ${previousBuildResult})"
        }

        if(config.channel == null || config.channel == "")
        {
            config.channel = "general"
        }

        // Default values
        def colorName = 'RED'
        def colorCode = '#FF0000'
        def icon = ':x: '

        // Override default values based on build status
        if (config.buildStatus == 'SUCCESS')
        {
            color = 'GREEN'
            colorCode = '#00CF00'
            icon = ':white_check_mark: '
        }
        else if (config.buildStatus == 'FAILURE')
        {
            color = 'RED'
            colorCode = '#FF0000'
            icon = ':x: '
        }
        else if (config.buildStatus == 'ABORTED')
        {
            color = 'GRAY'
            colorCode = '#AAAAAA'
            icon = ':grey_exclamation: '
        }
        else if (config.buildStatus == 'UNSTABLE')
        {
            color = 'YELLOW'
            colorCode = '#FFFACD'
            icon = ':warning: '
        }
        else if (config.buildStatus == 'START')
        {
            color = 'LIGHTGREEN'
            colorCode = '#7CFC00'
            icon = ':arrow_forward: '
        }
        else if (config.buildStatus == 'DONE')
        {
            color = 'LIGHTGREEN'
            colorCode = '#7CFC00'
            icon = ':white_check_mark: '
        }
        else
        {
            color = 'PINK'
            colorCode = '#FFCCCC'
            icon = ':x: '
        }

        def summary = "${icon}*${config.buildStatus}* ${prevBuildMessage} - " +
                "*<${env.BUILD_URL}|${env.JOB_NAME} - ${currentBuild.displayName}>*\n\n" +
                ">>> <${env.BUILD_URL}consoleFull|:jenkins_terminal: Build #${env.BUILD_NUMBER} console output>"
        if(config.buildStatus != 'START')
        {
            def elapsed = currentBuild.durationString.replace(" and counting","")
            summary+="\n:stopwatch: Elapsed time: *${elapsed}*"
        }
        def userFullName = getBuildUserFullName()
        if(isBuildStartedByUser())
        {
            summary+="\n:runner: Build triggered by *${userFullName}*"
        }
        else
        {
            summary+="\n:runner: Build run"
        }
        summary+=" on *${env.NODE_NAME}*"

        if(config.buildStatus == 'FAILURE' || config.buildStatus == 'UNSTABLE')
        {
            def changes = tplGetChangesList()
            if (changes != null && changes != "")
            {
                summary+="\n${changes}\n"
            }
        }

        if(config.additionalMessageText!=null && config.additionalMessageText != "")
        {
            summary+="\n${config.additionalMessageText}"
        }
        if (config.showParamsList != null && config.showParamsList == true)
        {
            if(params.size()>0)
            {
                summary+="\n> *Build parameters*:"
                Map<String, String> treeMap = new TreeMap<String, String>(params);
                for (parameter in treeMap)
                {
                    if(parameter.getValue() instanceof hudson.util.Secret)
                    {
                        summary+="\n> ["+parameter.getKey()+"] ********"
                    }
                    else
                    {
                        summary+="\n> ["+parameter.getKey()+"] *"+parameter.getValue()+"*"
                    }
                }
            }
        }

        slackSend (color: colorCode, message: summary, channel: "#${config.channel}" )
    }
    catch(error)
    {
        echo error.message
    }
}