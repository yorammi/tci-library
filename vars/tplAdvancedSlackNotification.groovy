def call(String buildStatus, String channel, String additionalMessageText = "")
{
    try
    {
        buildStatus = buildStatus ?: 'SUCCESS'

        // Default values
        def colorName = 'RED'
        def colorCode = '#FF0000'
        def icon = ':tired_face: '

        // Override default values based on build status
        if (buildStatus == 'STARTED')
        {
            color = 'YELLOW'
            colorCode = '#FFFF00'
            icon = ''
        }
        else if (buildStatus == 'SUCCESS')
        {
            color = 'GREEN'
            colorCode = '#00CF00'
            icon = ':+1: '
        }
        else if (buildStatus == 'FAILURE')
        {
            color = 'RED'
            colorCode = '#FF0000'
            icon = ':scream: '
        }
        else if (buildStatus == 'ABORTED')
        {
            color = 'GRAY'
            colorCode = '#AAAAAA'
            icon = ''
        }
        else
        {
            color = 'PINK'
            colorCode = '#FFCCCC'
            icon = ''
        }

        def summary = "${icon}*BUILD ${buildStatus}*\n\n[Job] *${env.JOB_NAME} #${env.BUILD_NUMBER}*\n[Name] *${currentBuild.displayName}*\n[Console] ${env.BUILD_URL}/consoleFull\n${additionalMessageText}"
        slackSend (color: colorCode, message: summary, channel: "#${channel}" )
    }
    catch(error)
    {
        echo "**** ${error.message}"
    }

}