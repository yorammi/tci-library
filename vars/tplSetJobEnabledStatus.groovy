def call(String jobName, boolean jobEnabledStatus) {
    try
    {
        if(jobEnabledStatus)
        {
            Jenkins.instance.getItem(jobName).doEnable()
            echo "The job '${jobName}' enabled"
        }
        else
        {
            Jenkins.instance.getItem(jobName).doDisable()
            echo "The job '${jobName}' disabled"
        }
    }
    catch (error)
    {
        echo error.message
    }

}
