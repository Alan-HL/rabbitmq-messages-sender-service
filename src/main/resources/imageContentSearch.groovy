import groovy.io.FileType
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.jsoup.nodes.Document

static def setup(){
    //Your ki manual folder
    File dir = new File("\\\\oembuild\\Chrysler\\Chrysler_35115_2019ChryslerJL_20210920_230936\\UIFilesUnzipped\\35115\\html")
    println("Found ${dir.listFiles().size()} files!")   //must match the number of files in the folder
    dir
}

static def parseFiles( inputFile) {
    Document doc = Jsoup.parse(inputFile, "UTF-8")
    Elements imgTags = doc.select("img")

    imgTags.each {
        if (!it.hasAttr("width"))
            println(inputFile.name.split("\\.html")[0])
    }
}

//Start
def dir = setup( )

dir.eachFileRecurse(FileType.FILES) { inputFile ->
    if(inputFile.name.contains("html")){
        parseFiles(inputFile)
    }
}