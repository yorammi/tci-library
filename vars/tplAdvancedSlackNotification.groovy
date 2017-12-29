def call(String buildStatus, String channel, String additionalMessageText = "")
{
    try
    {
        buildStatus = buildStatus ?: 'SUCCESS'

        // Default values
        def colorName = 'RED'
        def colorCode = '#FF0000'
        def icon = ':jenkins_red: '

        // Override default values based on build status
        if (buildStatus == 'STARTED')
        {
            color = 'LIGHTGREEN'
            colorCode = '#7CFC00'
            icon = ':start: '
        }
        else if (buildStatus == 'UNSTABLE')
        {
            color = 'YELLOW'
            colorCode = '#FFFF00'
            icon = ':jenkins_yellow: '
        }
        else if (buildStatus == 'SUCCESS')
        {
            color = 'GREEN'
            colorCode = '#00CF00'
            icon = ':jenkins_blue: '
        }
        else if (buildStatus == 'FAILURE')
        {
            color = 'RED'
            colorCode = '#FF0000'
            icon = ':jenkins_red: '
        }
        else if (buildStatus == 'ABORTED')
        {
            color = 'GRAY'
            colorCode = '#AAAAAA'
            icon = ':jenkins_aborted: '
        }
        else
        {
            color = 'PINK'
            colorCode = '#FFCCCC'
            icon = ':jenkins_red: '
        }

        def summary = "${icon}*BUILD ${buildStatus}*\n\n[Job] *${env.JOB_NAME} #${env.BUILD_NUMBER}*\n[Name] *${currentBuild.displayName}*\n[Console] ${env.BUILD_URL}/consoleFull\n${additionalMessageText}"
        slackSend (color: colorCode, message: summary, channel: "#${channel}" )
    }
    catch(error)
    {
        echo "**** ${error.message}"
    }

}