def call(Map config) {
    tciGeneral.deprecatedMessage("tplSmartSlackNotification","tciNotifications.sendSlackMessage")
    tciNotifications.sendSlackMessage(config)
}