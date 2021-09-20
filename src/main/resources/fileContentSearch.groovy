import groovy.io.FileType

static def setup(){
    //Your ki manual folder
    File dir = new File("\\\\oembuild\\Chrysler\\Chrysler_35133_2019ChryslerRU_20210917_140822\\UIFilesUnzipped\\35133\\html")
    println("Found ${dir.listFiles().size()} files!")   //must match the number of files in the folder
    dir
}

static def parseFiles( inputFile) {
    print("-")
    inputFile.eachLine {
        if (it.contains('Refer to 00')){    //What you want to search inside the html files
            println(inputFile.name)
        }
    }
    print("/")
}

//Start
def dir = setup( )

dir.eachFileRecurse (FileType.FILES) { inputFile ->
    parseFiles(inputFile)
}