def call() {

    try
    {
        docker.image("library/ubuntu").inside('-u root')
        {
            sh ("find ${WORKSPACE} -mindepth 1 -delete > /dev/null | true")
            sh ("chmod -R 777 ${WORKSPACE} > /dev/null | true")
        }
    }
    catch(error)
    {

    }
}