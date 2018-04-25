def call(String dir, Closure body)
{
    String directory = "."
//    if(flag)
//    {
        directory = dir
//    }
    dir("test")
    {
        body()
    }
}