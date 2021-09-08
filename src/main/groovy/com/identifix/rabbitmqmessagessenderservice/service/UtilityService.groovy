package com.identifix.rabbitmqmessagessenderservice.service

import com.identifix.kraken.client.KrakenClient
import com.identifix.kraken.client.bean.Credential
import com.identifix.kraken.client.bean.Manual
import com.identifix.kraken.client.bean.ManualPage
import com.identifix.rabbitmqmessagessenderservice.configuration.UtilityServiceConfig
import com.identifix.rabbitmqmessagessenderservice.proposal.MessageSender
import com.identifix.rabbitmqmessagessenderservice.utils.CommonConstants
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.ZonedDateTime
import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
@Slf4j
class UtilityService {
    @Autowired
    MessageSender messageSender

    @Autowired
    UtilityServiceConfig utilityServiceConfig

    @Lazy(soft=true)
    KrakenClient krakenClient = {
        Credential credential = new Credential(securityServiceUrl:utilityServiceConfig.securityServiceUri,
                clientId:utilityServiceConfig.clientId,
                clientSecret:utilityServiceConfig.clientSecret)

        new KrakenClient(krakenServiceUrl:utilityServiceConfig.krakenUri, credential:credential)
    } ()

    String validateManualPagesDate(String manualUid, String limitDate, String exchangeName) {
        Manual manual = krakenClient.getManualById(manualUid)
        byte[] content = krakenClient.getManualBytes(manual)

        JSONArray parsedManual = toJSONArray(content)
        List<ManualPage> manualPages = getManualPages(parsedManual)

        ZonedDateTime limit = ZonedDateTime.parse("${limitDate}T00:00:00.000Z[UTC]")
        int outdatedPages = 0
        String response = ""

        manualPages.each {
            if (it.freshness.isBefore(limit)) {
                response += "MetaLinkId: ${it.publisherDocumentId} with freshness ${it.freshness}\n"
                log.info("MetaLinkId :${it.publisherDocumentId} with freshness ${it.freshness}")
                outdatedPages++
            }
        }

        response += "${outdatedPages} outdated pages"

        obtainAndSendRabbitMessages(response, exchangeName)
        response
    }

    static JSONArray toJSONArray(byte[] content) {
        Document document = Jsoup.parse(new String(content), CommonConstants.UTF_8, Parser.xmlParser())
        JSONArray array = new JSONArray()
        document.select(CommonConstants.ITEM).each { item ->
            if (item.hasAttr(CommonConstants.PAGECODE)) {
                JSONArray object = new JSONArray()
                object.put(item.attr(CommonConstants.PAGECODE))
                String tocPath = extractToc(item.attr(CommonConstants.TITLE))
                object.put(tocPath)
                object.put(item.attr(CommonConstants.TITLE).replace(tocPath, "").trim())
                array.put(object)
            }
        }
        array
    }
    static String extractToc(String title) {
        Pattern pattern = Pattern.compile("\"\\\\[(.*?)\\\\]\"", Pattern.CASE_INSENSITIVE)
        Matcher matcher = pattern.matcher(title)
        matcher.find() ? matcher.group() : title
    }

    List<ManualPage> getManualPages(JSONArray parsedManual) {
        int missingDocumentsNumber = 0
        List<String> missingDocuments = []
        String publisherDocumentId
        List<ManualPage> pages = []

        for (int i = 0; i < parsedManual.length(); i++) {
            try {
                publisherDocumentId = (parsedManual.get(i) as JSONArray).get(0)
                ManualPage page = krakenClient.getManualPageByPublisherDocumentId(publisherDocumentId)

                if (!page) {
                    log.error "Error - Found null value for page."
                    continue
                }
                pages.add(page)
                log.info("Success Found Nuxeo page ${publisherDocumentId} Number: ${pages.size()}")
            } catch (Exception e) {
                missingDocumentsNumber++
                missingDocuments.add(publisherDocumentId)
                log.error("Error when retrieving manual page from Nuxeo: ${publisherDocumentId}: ${e.message}")
            }
        }
        log.info("Number of missing pages: ${missingDocumentsNumber}")
        missingDocuments.each {
            log.info("MetaLinkId : ${it}")
        }

        pages
    }

    void obtainAndSendRabbitMessages( String pages, String exchangeName) {
        Map<String,String> allMessages = loadAllMessagesToMap()
        List<String> messagesToBeSent = []

        pages.eachLine {
            if (it.contains('MetaLinkId: ')) {
                String metaLinkId = it.split(" ")[1]
                messagesToBeSent.add(allMessages.get(metaLinkId))
            }
        }
        log.info(messagesToBeSent.size() as String)

        messageSender.sendMessages(messagesToBeSent, exchangeName)
    }

    Map<String,String> loadAllMessagesToMap() {
        File inputFile = new File("Rabbit-Messages.txt")
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map<String,String> pagesMap = [:]
        inputFile.eachLine {line ->
            Object object = jsonSlurper.parseText(line)
//        println object.metaLinkId
            pagesMap.put(object.metaLinkId, line)
        }
        println("Total Rabbit Messages: ${pagesMap.size()}")

        pagesMap
    }
}
