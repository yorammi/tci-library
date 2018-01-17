def call() {
    try
    {
        MAX_MSG_LEN = 40
        def changeString = ""
        def changeLogSets = currentBuild.changeSets

        def remoteConfigs = scm.getUserRemoteConfigs()
        def scmUrl = remoteConfigs[0].getUrl()
        repoUrl = scmUrl.take(scmUrl.size()-4)
        scmCommitPrefix = repoUrl+"/commit/"
        repoName = repoUrl.drop(repoUrl.lastIndexOf("/")+1)
        if(changeLogSets.size()>0)
        {
            changeString="> *SCM changes*:"
            for (int i = 0; i < changeLogSets.size(); i++) {
                changeString+="\n> _Repository (${i+1}):_"
                def entries = changeLogSets[i].items
                for (int j = 0; j < entries.size(); j++) {
                    if(j<5)
                    {
                        def entry = entries[j]
                        truncated_msg = entry.msg.take(MAX_MSG_LEN)
                        def emailAddress = entry.authorEmail
                        def slackUser = emailAddress.substring(0, emailAddress.lastIndexOf("@"))

                        def hash = entry.getCommitId()
                        def hashShort = hash.take(8)
                        changeString += "\n> ${hashShort} - *${truncated_msg}* - @${slackUser}"
                    }
                    else
                    {
                        if(j==3)
                        {
                            changeString+="\n..."
                        }
                    }
                }
            }
        }
        return changeString
    }
    catch (Exception error)
    {
        println(error.getMessage())
        return ""
    }
}