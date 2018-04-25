def call(boolean flag, String dir, Closure body)
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