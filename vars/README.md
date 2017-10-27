# ***tikal-pipelib available tasks***
![tikal-pipelib](../src/resources/images/TPL-small.png)

Powered by **[Tikal Knowledge](http://www.tikalk.com)** and the community.
<hr/>

## TPL_advancedSlackNotification

***Send a well-formatted Slack notification***

#### Task usage

TPL_advancedSlackNotification(arguments)

Arguments:

| Argument name and type | Description | Default Value |
| ------------- | ----------- | ------------- |
| String buildStatus| Build status| N/A|
| String channel| Slack channel| N/A|
| String additionalMessageText| Additional text to the notification message| empty text|

#### Example
TPL_advancedSlackNotification ("SUCCESS","test-channel","@here")

## TPL_echo

***Echo text with time-stamp***

#### Task usage

TPL_echo(arguments)

Arguments:

| Argument name and type | Description | Default Value |
| ------------- | ----------- | ------------- |
| String text| Text to display| N/A|

#### Example
TPL_echo ("Hello TPL!")

#### Output
[20170715-05:40:11.393] Hello TPL!

## TPL_getBuildUserId

***Get job acticator user-id***

#### Task usage example

def userId = TPL_getBuildUserId()

## TPL_setStatusByLogText

***set the build status based on searched text in the build log file***

#### Task usage

TPL_setStatusByLogText(Arguments)

Arguments:

| Argument name and type | Description | Default Value |
| ------------- | ----------- | ------------- |
| String searchText| Text to search| N/A|

#### Example
TPL_setStatusByLogText ("ERROR")

#### Output
Found 'ERROR' in build log
