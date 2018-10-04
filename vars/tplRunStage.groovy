def call(String name, Closure stage) {
    if (currentBuildResult in ['SUCCESS', null]) {
        echo "Start stage $name"
        stage(name, stage)
        echo "End stage $name with result ${currentBuildResult ?: 'SUCCESS'}"
    } else {
        stage(name) {
            echo "Build is unstable, skipping stage $name"
        }
    }
}
