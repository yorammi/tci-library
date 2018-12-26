import hudson.model.User

def call() {
    tciGeneral.deprecatedMessage("tplGetBuildUserFullName","tciBuildUser.getBuildUserDisplayName")
    return tciBuildUser.getBuildUserDisplayName()
}