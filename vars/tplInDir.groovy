def call(Closure body,String dir)
{
    String directory = "."
//    if(flag)
//    {
        directory = dir
//    }
    dir(directory)
    {
        body()
    }
}