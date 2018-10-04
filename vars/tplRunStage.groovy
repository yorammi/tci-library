def call(String name, Closure stage) {
    if (currentBuild.currentResult in ['SUCCESS', null]) {
        echo "Start stage $name"
        stage(name, stage)
        echo "End stage $name with result ${currentBuild.currentResult ?: 'SUCCESS'}"
    } else {
        stage(name) {
            echo "Build is unstable, skipping stage $name"
        }
    }
}
