package com.identifix.rabbitmqmessagessenderservice.proposal

import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.lang.annotation.Documented

@Component
@Slf4j
class FilesProcessor {

    @Autowired
    MessageSender messageSender

    void generateToc(String files) {
        try {
            String pattern = 'pageCode="NotFound'
            List pages = []
            File file
            files.split("\r\n").eachWithIndex { fileName, i ->
                try {
                    log.info("opening file $i, name: $fileName")
                    if(i == 0){
                        file = new File(fileName)
                        file.eachLine { line ->
                            if (line.contains(pattern)) {
                                String page = line.split(pattern)[1].split("\\.xml")[0]
                                log.info(page)
                                if(!pages.contains(page)){
                                    pages.add(page)
                                }
                            }
                        }
                        log.info("pages: ${pages.size()}")
                    }
                    else {
                        file = new File(fileName)
                        Document document = Jsoup.parse(new String(file.getBytes()), "utf-8", Parser.xmlParser())
                        document.select("servinfo").eachWithIndex{ Element item, int j ->
                            String id = item.attr("id")
                            if(!pages.contains(id)){
                                item.remove()
                            }
                        }
                        document.getAllElements().eachWithIndex{ Element item, int j ->
                            if(!item.getElementsByTag("ptxt") && item.childNodeSize() > 1){
                                item.remove()
                            }
                        }
                        getPaths(document)
                        String newToc = new String(document.html().bytes)
                        String outputPath = "src/main/resources/newToc.xml"
                        createFile(newToc, outputPath)

                    }
                }
                catch (Exception e) {
                    log.error("error: $e")
                }
            }
        }
        catch (Exception e) {
            log.error("error: $e")
        }
    }

    void createFile(String content, String outputPath)
    {
        String fileName = outputPath
        log.info("creating file: $fileName")
        File file = new File(fileName)
        messageSender.validateFile(file)
        FileWriter writeFile = new FileWriter(file, true)
        writeFile.write("$content \n")
        writeFile.close()
        log.info("endend create new toc")
    }

    void getPaths(Document document){
        String system
        String subsystem
        String component
        String infotype
        String servinfo
        String tocPath
        String salescode
        String salesSystem
        String salesComponent
        String salesSubsystem
        String tocPaths
        tocPaths = ""
        document.getAllElements().eachWithIndex{ Element item, int j ->
            if(item.tagName() =="system"){
                if(item.hasAttr("salescode")){
                    salesSystem = item.attr("salescode")
                }
                else{
                    salesSystem = ""
                }
            }
            if(item.tagName() =="systemname"){
                system = item.getElementsByTag("systemname").text().replace(",","")
            }

            if(item.tagName() =="subsystem"){
                if(item.hasAttr("catnbr")){
                    subsystem = item.attr("catnbr")
                }
                else{
                    subsystem = "0000"
                }
                if(item.hasAttr("salescode")){
                    salesSubsystem = item.attr("salescode")
                }
                else{
                    salesSubsystem = ""
                }
            }
            if(item.tagName() =="subsystemname"){
                subsystem = "$subsystem ${item.getElementsByTag("subsystemname").text().replace(",","")}"
            }


            if(item.tagName() == "component"){
                if(item.hasAttr("compid")){
                    component = item.attr("compid")
                }
                else{
                    component = "00000"
                }
                if(item.hasAttr("salescode")){
                    salesComponent = item.attr("salescode")
                }
                else{
                    salesComponent = ""
                }
            }
            if(item.getElementsByTag("componentname")){
                component = "$component ${item.getElementsByTag("componentname").text().replace(",","")}"
            }


            if(item.getElementsByTag("infotype")){
                if(item.hasAttr("infotypevalue")){
                    infotype = item.attr("infotypevalue")
                }
                else{
                    infotype = ""
                }
            }
            if(item.getElementsByTag("infotypename")){
                infotype = "$infotype ${item.getElementsByTag("infotypename").text().replace(",","")}"
            }


            if(item.getElementsByTag("servinfo")){
                servinfo = item.getElementsByTag("title").text().replace(",","")
            }

            if(item.tagName() == "ptxt"){
                salescode = obtainSalescode(salesSystem, salesSubsystem, salesComponent)
                tocPath = (salescode == "" ) ?
                        "$system, $subsystem, $component, $infotype, $servinfo" :
                        "$system, $subsystem, $component, $infotype, $servinfo, $salescode"
                println(tocPath)
                tocPaths = "$tocPaths $tocPath\n"
            }
        }
        String outputPath = "src/main/resources/tocPaths.txt"
        createFile(tocPaths, outputPath)

    }

    String obtainSalescode(String salescodeSystem, String salescodeSubsystem, String salescodeComponent) {
        String salescode = ""
        if (salescodeComponent != "" && salescodeComponent != null) {
            salescode = salescodeComponent }
        if (salescodeSubsystem  != "" && salescodeSubsystem  != null) {
            salescode = salescodeSubsystem }
        if (salescodeSystem != "" && salescodeSystem != null ) {
            salescode = salescodeSystem }

        salescode
    }
}
