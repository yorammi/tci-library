# TCI library - Tikal Jenkins-based CI solution Jenkins shared library
![tci-library](src/resources/images/tci-library.png)

### ***TCI library - Tikal Jenkins-based CI solution Jenkins shared library***

Powered by **[Tikal Knowledge](http://www.tikalk.com)** and the community.
<hr/>

**tikal-pipelib** is a [shared library](https://jenkins.io/doc/book/pipeline/shared-libraries/) for [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/).

The Library is a collection of tasks and advanced flows that can be used inside a pipeline.

Anyone who wants to contribute to this library - please follow the instructions below in the page.

For tests of this repository and Jenkins pipeline examples, see: [tikal-pipelib-touchstone](https://github.com/tikalk/tikal-pipelib-touchstone).

## [Available tasks](vars/README.md)

* tplAdvancedSlackNotification
* tplEcho
* tplGetBuildUserId
* tplSetStatusByLogText
* tplKeepBuildForever

## [Available flows](src/tpl/ci)

* [tplBaseCiPipeline](src/tpl/ci/tplBaseCiPipeline.groovy)
  * [tplGradleCiPipeline](src/tpl/ci/gradle/tplGradleCiPipeline.groovy)
  * [tplArtifactoryCiPipeline](src/tpl/ci/maven/tplArtifactoryCiPipeline.groovy)

## Adding an item to tikal-pipelib

For adding a new task, please follow those steps.

1. Create a branch or a fork from the master branch.
2. Write the groovy file for the task in the [/vars](/vars) folder - the file name **MUST** start with **tpl** prefix.
4. Write a markdown section for describing the added task in [/vars/README.md](/vars/README.md) file.
5. commit and push your branch.
6. Submit a pull request.

Please notice that only the following of these instructions will be accepted.

Tikal reserve the right to accept or reject pull requests.
