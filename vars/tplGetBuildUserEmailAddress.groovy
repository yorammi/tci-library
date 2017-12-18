import hudson.tasks.Mailer
import hudson.model.User

def call() {
    try{
        def userId = tplGetBuildUserId()
        if("" != userId)
        {
            User user = User.get(userId)
            def userMail = user.getProperty(Mailer.UserProperty.class)
            def userEmailAddress = userMail.getAddress()
            return userEmailAddress
        }
        else
        {
            return ""
        }
    }
    catch (Exception error)
    {
        println(error.getMessage())
        return ""
    }
}