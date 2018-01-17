import hudson.model.User

def call() {
    try{
        def userId = tplGetBuildUserId()
        if("" != userId)
        {
            User user = User.get(userId)
            def userFullName = user.getFullName()
            return userFullName
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