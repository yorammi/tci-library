def call(String repositoryUrl, String branch, String credentialsId, String relativPath=".") {
    tciGeneral.deprecatedMessage("tplRepositoryDirectoryCheckout","tciJobs.setRandomBuildStatus")
    tciGit.gitCheckout(repoUrl: repositoryUrl, branch: branch,credentialsId: credentialsId, dir: relativPath)
}

