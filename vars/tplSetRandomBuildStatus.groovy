#!/usr/bin/groovy

@NonCPS
def call() {

    try
    {
        randomStatus = "SUCCESS"
        randomNumber = Math.abs(new Random().nextInt() % 4)
        switch (randomNumber)
        {
            case 0:
                randomStatus = "SUCCESS"
                break;
            case 1:
                randomStatus = "UNSTABLE"
                break;
            case 2:
                randomStatus = "FAILURE"
                break;
            case 3:
                randomStatus = "ABORTED"
                break;
            default:
                randomStatus = "SUCCESS"
                break;
        }
        currentBuild.result = randomStatus
    }
    catch(error)
    {
        echo error.message
    }
}
