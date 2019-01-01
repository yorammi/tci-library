def call(String buildStatus, String channel, String additionalMessageText = "") {
    tciGeneral.deprecatedMessage("tplAdvancedSlackNotification","tciNotifications.sendSlackMessage")
    tciNotifications.sendSlackMessage(buildStatus:buildStatus, channel:channel, additionalMessageText:additionalMessageText)
}