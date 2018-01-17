def call() {
    try
    {
        List<String> changersString = []
        def changeLogSets = currentBuild.changeSets

        def remoteConfigs = scm.getUserRemoteConfigs()
        def scmUrl = remoteConfigs[0].getUrl()
        repoUrl = scmUrl.take(scmUrl.size()-4)
        scmCommitPrefix = repoUrl+"/commit/"
        repoName = repoUrl.drop(repoUrl.lastIndexOf("/")+1)
        if(changeLogSets.size()>0)
        {
            for (int i = 0; i < changeLogSets.size(); i++) {
                def entries = changeLogSets[i].items
                for (int j = 0; j < entries.size(); j++) {
                    def entry = entries[j]
                    def emailAddress = entry.authorEmail
                    def slackUser = emailAddress.substring(0, emailAddress.lastIndexOf("@"))
                    changersString.add(slackUser)
                }
            }
        }
        return changersString.unique()
    }
    catch (Exception error)
    {
        println(error.getMessage())
        return null
    }
}