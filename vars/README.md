# ***tikal-pipelib available tasks***
![tikal-pipelib](../src/resources/images/TPL-small.png)

Powered by **[Tikal Knowledge](http://www.tikalk.com)** and the community.
<hr/>

## tplAdvancedSlackNotification

***Send a well-formatted Slack notification***

#### Task usage

tplAdvancedSlackNotification(arguments)

Arguments:

| Argument name and type | Description | Default Value |
| ------------- | ----------- | ------------- |
| String buildStatus| Build status| N/A|
| String channel| Slack channel| N/A|
| String additionalMessageText| Additional text to the notification message| empty text|

#### Example
tplAdvancedSlackNotification ("SUCCESS","test-channel","@here")

## tplEcho

***Echo text with time-stamp***

#### Task usage

tplEcho(arguments)

Arguments:

| Argument name and type | Description | Default Value |
| ------------- | ----------- | ------------- |
| String text| Text to display| N/A|

#### Example
tplEcho ("Hello TPL!")

#### Output
[20170715-05:40:11.393] Hello TPL!

## tplGetBuildUserId

***Get job acticator user-id***

#### Task usage example

def userId = tplGetBuildUserId()

## tplSetStatusByLogText

***set the build status based on searched text in the build log file***

#### Task usage

tplSetStatusByLogText(Arguments)

Arguments:

| Argument name and type | Description | Default Value |
| ------------- | ----------- | ------------- |
| String searchText| Text to search| N/A|

#### Example
tplSetStatusByLogText ("ERROR")

#### Output
Found 'ERROR' in build log

## tplKeepBuildForever

***set the current build to be kept forever and not be deleted by any rule except for manual deletion***

#### Task usage

tplKeepBuildForever()

