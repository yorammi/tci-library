#!/usr/bin/groovy

def gitCheckout(Map config) {
    try {
        if (!config) {
            config = [:]
        }
        if (!config.repoUrl || config.repoUrl == "") {
            echo "[ERROR] repository URL must be provided!"
            currentBuild.result = "FAILURE"
            return
        }
        if (!config.branch) {
            config.branch = "master"
        }
        if (!config.credentialsId) {
            if (env.TCI_MASTER_DEFAULT_GIT_CREDENTIAL) {
                config.credentialsId = env.TCI_MASTER_DEFAULT_GIT_CREDENTIAL
            }
            else {
                config.credentialsId = "gitsshkey"
            }
        }
        if (!config.dir) {
            config.dir = "."
        }

        dir(config.dir) {
            timeout(time: 5, unit: 'MINUTES') {
                checkout([
                        $class: 'GitSCM', branches: [[name: config.branch]],
                        userRemoteConfigs: [[url: config.repoUrl ,credentialsId:config.credentialsId]]
                ])
            }
        }
    }
    catch (error) {
        echo "[ERROR] ${error}"
    }
}

@NonCPS
def getChangesList() {
    try
    {
        MAX_MSG_LEN = 120
        def changeString = ""
        def changeLogSets = currentBuild.changeSets

        def remoteConfigs = scm.getUserRemoteConfigs()
        def scmUrl = remoteConfigs[0].getUrl()
        repoUrl = scmUrl.take(scmUrl.size()-4)
        scmCommitPrefix = repoUrl+"/commit/"
        repoName = repoUrl.drop(repoUrl.lastIndexOf("/")+1)
        if(changeLogSets.size()>0)
        {
            changeString="[SCM changes]"
            for (int i = 0; i < changeLogSets.size(); i++) {
                changeString+="\n\t[Repository (${i+1})]"
                def entries = changeLogSets[i].items
                for (int j = 0; j < entries.size(); j++) {
                    def entry = entries[j]
                    truncated_msg = entry.msg.take(MAX_MSG_LEN)
                    def emailAddress = entry.authorEmail
                    def emailUser = emailAddress.substring(0, emailAddress.lastIndexOf("@"))

                    def hash = entry.getCommitId()
                    def hashShort = hash.take(10)
                    changeString += "\n\t\t[${hashShort}@${emailUser}] ${truncated_msg}"
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

@NonCPS
def getChangersList() {
    try
    {
        def changersString = ""
        def changeLogSets = currentBuild.changeSets

        tciLogger.debug("2")
        def remoteConfigs = scm.getUserRemoteConfigs()
        tciLogger.debug("3")
        def scmUrl = remoteConfigs[0].getUrl()
        tciLogger.debug("4")
        repoUrl = scmUrl.take(scmUrl.size()-4)
        tciLogger.debug("5")
        scmCommitPrefix = repoUrl+"/commit/"
        tciLogger.debug("6")
        repoName = repoUrl.drop(repoUrl.lastIndexOf("/")+1)
        tciLogger.debug("7")
        if(changeLogSets.size()>0)
        {
            tciLogger.debug("8")
            for (int i = 0; i < changeLogSets.size(); i++) {
                tciLogger.debug("9")
                def entries = changeLogSets[i].items
                tciLogger.debug("10")
                for (int j = 0; j < entries.size(); j++) {
                    tciLogger.debug("11")
                    def entry = entries[j]
                    tciLogger.debug("12")
                    def emailAddress = entry.authorEmail
                    tciLogger.debug("13")
                    def emailUser = emailAddress.substring(0, emailAddress.lastIndexOf("@"))
                    tciLogger.debug("14")
                    if(changersString=="") {
                        changersString = emailUser
                    }
                    else {
                        changersString += ","+emailUser
                    }
                    tciLogger.debug("15")
                }
            }
        }
        tciLogger.debug("16")
        return changersString
    }
    catch (Exception error)
    {
        return ""
    }
}

