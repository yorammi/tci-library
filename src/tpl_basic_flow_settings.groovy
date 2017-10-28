class tpl_basic_flow_settings implements Serializable {
    private String flow
    private String gitUrl

    def setFlow(value) {
        flow = value
    }
    def getFlow() {
        flow
    }
    def setGitUrl(value) {
        gitUrl = value
    }
    def getGitUrl() {
        gitUrl
    }
}
