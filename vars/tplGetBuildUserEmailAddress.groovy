import hudson.tasks.Mailer
import hudson.model.User

def call() {
    tciGeneral.deprecatedMessage("tplGetBuildUserEmailAddress","tciBuildUser.getBuildUserEmail")
    return tciBuildUser.getBuildUserEmail()
}