import groovy.io.FileType

static def setup(){
    //Your ki manual folder
    File dir = new File("\\\\oembuild\\Chrysler\\Chrysler_28460_2017ChryslerJK_20210920_194325\\UIFilesUnzipped\\28460\\html")
    println("Found ${dir.listFiles().size()} files!")   //must match the number of files in the folder
    dir
}

static def parseFiles( inputFile) {
    int i = 0
    inputFile.eachLine {
        if (it.contains('img') && !it.contains("width") && i==0){
            println(inputFile.name.split("\\.html")[0])
            i++
        }
    }
}

//Start
def dir = setup( )

dir.eachFileRecurse (FileType.FILES) { inputFile ->
    if(inputFile.name.contains("html")){
        parseFiles(inputFile)
    }
}