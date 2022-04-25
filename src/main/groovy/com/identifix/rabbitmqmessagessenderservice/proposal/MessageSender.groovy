package com.identifix.rabbitmqmessagessenderservice.proposal

import groovy.util.logging.Slf4j
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class MessageSender {

    @Autowired
    RabbitTemplate rabbitTemplate;

    void processFile( String exchangeName, String files) {
        try {
            List messages = obtainMessages(files)
            println("total messages:${messages.size()}")
            sendMessages(messages,exchangeName)
        }
        catch (Exception e) {
            log.error("error: $e")
        }
    }

    void processFiles( String exchangeName, String files) {
        try {
            String pattern = 'publisherDocumentID: '
            List messages = []
            File file
            files.split("\r\n").eachWithIndex { fileName, i ->
                try {
                    log.info("opening file $i, name: $fileName")
                    if(i == 0){
                        file = new File(fileName)
                        file.eachLine { line ->
                            if (line.contains(pattern)) {
                                String message = line.split("publisherDocumentID: ")[1].split(" Total")[0]
                                log.info(message)
                                messages.add(message)
                            }
                        }
                    }
                    else {
                        List finalMessages = obtainMatchMessages(fileName, messages)
                        println("total messages:${messages.size()}")
                        sendMessages(finalMessages,exchangeName)
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

    void processQueue( String queueName, filePath ) {
        try {
            readAndSaveMessages(queueName, filePath)

        }
        catch (Exception e) {
            log.error("error: $e")
        }
    }

    static List obtainMatchMessages(files, messageList) {
        String pattern = 'Listener received message <{'
        List messages = []
        File file
        int messagesNumber = 0
        files.split("\r\n").eachWithIndex { fileName, i ->
            try {
                log.info("opening file $i to match messages, name: $fileName")
                file = new File(fileName)
                file.eachLine { line ->
                    if (line.contains(pattern)) {
                        String metaLink = line.split("uuid\":\"")[1].split("\"")[0]
                        if(messageList.contains(metaLink)){
                            log.info("metalink: " + metaLink)
                            String message = line.split("Listener received message <")[1].split("> from")[0]
                            log.info(message)
                            messages.add(message)
                            messagesNumber++
                        }
                    }
                }
            }
            catch (Exception e) {
                log.error("error: $e")
            }
        }
        log.info("Messages found: $messagesNumber\n")
        return messages
    }

    static List<String> obtainMessages(String files) {
        String pattern = 'Listener received message <{"'
        List<String> messages = []
        File file
        int messagesNumber = 0
        files.split("\r\n").eachWithIndex { fileName, i ->
            try {
                log.info("opening file $i, name: $fileName")
                file = new File(fileName)
                file.eachLine { line ->
                    if (line.contains(pattern) && line.contains("FORD_WORKSHOP_PAGE")) {
                        String message = line.split("Listener received message <")[1].split("> from")[0]
                        log.info(message)
                        messages.add(message)
                        messagesNumber++
                    }
                }
            }
            catch (Exception e) {
                log.error("error: $e")
            }
        }
        log.info("Messages found: $messagesNumber\n")
        return messages
    }

    void sendMessages(List messages, String exchangeName) {
        try {
            messages.forEach { message ->
                rabbitTemplate.convertAndSend(exchangeName, "", message.toString());
            }
        }
        catch (Exception e) {
            log.error("error: $e")
        }
    }

    void readAndSaveMessages(String queueName, String filePath)
    {
        log.info("creating file: $filePath")
        File file = new File(filePath)
        validateFile(file)
        FileWriter writeFile = new FileWriter(file, true)
        log.info("reading messages")
        String message = rabbitTemplate.receiveAndConvert(queueName)
        while (message) {
            println(message)
            writeFile.write("$message \n")
            message = rabbitTemplate.receiveAndConvert(queueName)
        }
        writeFile.close()
        log.info("endend messages")
    }

    private static void validateFile(File file) {
        if (file.exists()) {
            file.delete()
            file.createNewFile()
        }
    }
}
