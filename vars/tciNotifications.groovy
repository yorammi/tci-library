#!/usr/bin/env groovy

def sendSlackNotification(Map config) {
    try
    {
        if (config == null) {
            config = [:]
        }
        if (config.message == null) {
            config.message = ''
        }

        if (config.channel == null) {
            config.channel = '#general'
        }
        else {
            if (!config.channel.startsWith("#") && !config.channel.startsWith("@")) {
                config.channel = '#'+config.channel
            }
        }


        if (config.additionalMessageText == null)
        {
            config.additionalMessageText = ''
        }
        if (config.notifyOnSuccess == null)
        {
            config.notifyOnSuccess = false
        }
        if (config.hideJobName == null)
        {
            config.hideJobName = false
        }
        if (config.hideElapsedTime == null)
        {
            config.hideElapsedTime = false
        }

        def previousBuildResult = null
        try
        {
            previousBuildResult=currentBuild.rawBuild.getPreviousBuild()?.getResult()
        }
        catch (error)
        {
            echo "[ERROR] "+error.message
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
            prevBuildMessage = "(was: ${previousBuildResult})"
        }

        // Default values
        def colorName = 'RED'
        def colorCode = '#FF0000'
        def icon = ':jenkins_red: '

        if (config.buildStatus == 'SUCCESS')
        {
            color = 'GREEN'
            colorCode = '#00CF00'
            icon = ':jenkins_blue: '
        }
        else if (config.buildStatus == 'FAILURE')
        {
            color = 'RED'
            colorCode = '#FF0000'
            icon = ':jenkins_red: '
        }
        else if (config.buildStatus == 'ABORTED')
        {
            color = 'GRAY'
            colorCode = '#AAAAAA'
            icon = ':jenkins_gray: '
        }
        else if (config.buildStatus == 'UNSTABLE')
        {
            color = 'YELLOW'
            colorCode = '#FFFACD'
            icon = ':jenkins_yellow: '
        }
        else if (config.buildStatus == 'START')
        {
            color = 'LIGHTGREEN'
            colorCode = '#7CFC00'
            icon = ':start: '
        }
        else if (config.buildStatus == 'DONE')
        {
            color = 'LIGHTGREEN'
            colorCode = '#7CFC00'
            icon = ':+1: '
        }
        else
        {
            color = 'PINK'
            colorCode = '#FFCCCC'
            icon = ':jenkins_red: '
        }

        def summary = "${icon}*${config.buildStatus}* ${prevBuildMessage}"
        if (!config.hideJobName)
        {
            if(config.alternateJobTitle == null || config.alternateJobTitle == "")
            {
                summary+=" - *${env.JOB_NAME}*"
            }
            else
            {
                summary+=" - *${config.alternateJobTitle}*"
            }
        }
        summary+="\n>>>:book: <${env.BUILD_URL}| ${currentBuild.displayName}>"
        summary+=" <${env.BUILD_URL}consoleFull|:jenkins_terminal:>"
        try
        {
            if(currentBuild.description != null && "${currentBuild.description}" != "" && config.showDescription)
            {
                summary+="\n:memo: ${currentBuild.description}"
            }
        }
        catch (error)
        {
            echo "[ERROR] "+error.message
        }
        summary+="\n"
        def nodeName = env.NODE_NAME
        if(nodeName == "master")
        {
            nodeName = "(master)"
        }
        summary+=" :computer: ${env.NODE_NAME}"
        if(config.buildStatus != 'START' && !config.hideElapsedTime)
        {
            def elapsed = currentBuild.durationString.replace(" and counting","")
            summary+=" :stopwatch: *${elapsed}*"
        }

        if(env.TESTS_SUMMARY!=null && "${env.TESTS_SUMMARY}" != "")
        {
            summary+="\n`${env.TESTS_SUMMARY}`"
            summary+="\n:jenkins_clipboard: <${env.BUILD_URL}testReport|*JUnit* tests report>"
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

        try
        {
            slackSend (color: colorCode, message: summary, channel: "${config.channel}" )
        }
        catch (error3)
        {
            echo "[ERROR] "+error3.message
        }
    }
    catch(error)
    {
        echo "[ERROR] "+error.message
    }
}

def sendEmailNotification(Map config) {
    try
    {
        if (config == null) {
            config = [:]
        }


        if (config.message == null) {
            config.message = ''
        }

        if (config.from == null) {
            config.from = ''
        }

        if (config.replyTo == null) {
            config.replyTo = ''
        }

        if (config.to == null) {
            config.to = ''
        }

        if (config.subject == null) {
            config.subject = ''
        }

        if (config.additionalMessageText == null)
        {
            config.additionalMessageText = ''
        }
        if (config.notifyOnSuccess == null)
        {
            config.notifyOnSuccess = false
        }
        if (config.hideJobName == null)
        {
            config.hideJobName = false
        }
        if (config.hideElapsedTime == null)
        {
            config.hideElapsedTime = false
        }

        def previousBuildResult = null
        try
        {
            previousBuildResult=currentBuild.rawBuild.getPreviousBuild()?.getResult()
        }
        catch (error)
        {
            echo "[ERROR] "+error.message
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
            prevBuildMessage = "(was: ${previousBuildResult})"
        }

        // Default values
        def colorName = 'RED'
        def colorCode = '#f9bdbd'

        if (config.buildStatus == 'SUCCESS')
        {
            color = 'GREEN'
            colorCode = '#eff995'
        }
        else if (config.buildStatus == 'FAILURE')
        {
            color = 'RED'
            colorCode = '#f9bdbd'
        }
        else if (config.buildStatus == 'ABORTED')
        {
            color = 'GRAY'
            colorCode = '#AAAAAA'
        }
        else if (config.buildStatus == 'UNSTABLE')
        {
            color = 'YELLOW'
            colorCode = '#FFFACD'
        }
        else if (config.buildStatus == 'START')
        {
            color = 'LIGHTGREEN'
            colorCode = '#7CFC00'
        }
        else if (config.buildStatus == 'DONE')
        {
            color = 'LIGHTGREEN'
            colorCode = '#7CFC00'
        }
        else
        {
            color = 'PINK'
            colorCode = '#FFCCCC'
        }

        config.subject = "[${config.buildStatus}] ${prevBuildMessage}"
        def summary = "<body bgcolor='"+colorCode+"'>"
        summary += "<H1>${config.buildStatus} ${prevBuildMessage}"
        if (!config.hideJobName)
        {
            if(config.alternateJobTitle == null || config.alternateJobTitle == "")
            {
                summary+=" [${env.JOB_NAME}]"
                config.subject+=" [${env.JOB_NAME}]"
            }
            else
            {
                summary+=" [${config.alternateJobTitle}]"
                config.subject+=" [${config.alternateJobTitle}]"
            }
        }
        config.subject+=" [${currentBuild.displayName}]"
        summary+=" [<A HREF='${env.BUILD_URL}'>${currentBuild.displayName}</A>]</H1>"
        if(config.additionalMessageText!=null && config.additionalMessageText != "")
        {
            summary+="<p>${config.additionalMessageText}"
        }

        try
        {
            echo "subject: "+config.subject
            echo "summary: "+summary
            emailext (subject: config.subject,
                    body: summary,
                    mimeType: 'text/html',
                    from: config.from,
                    replyTo: config.replyTo,
                    to: config.to)
        }
        catch (error3)
        {
            echo "[ERROR] "+error3.message
        }
    }
    catch(error)
    {
        echo "[ERROR] "+error.message
    }
}
