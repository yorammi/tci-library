def call(Closure body, boolean flag, String dir)
{
    def directory = "."
    if(flag)
    {
        directory = dir
    }
    dir(directory)
    {
        body()
    }
}