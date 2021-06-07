package com.identifix.rabbitmqmessagessenderservice.proposal

import groovy.json.JsonGenerator
import groovy.json.JsonSlurper
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

    static List obtainMessages(files) {
        String pattern = 'Sending message'
        List messages = []
        File file
        int messagesNumber = 0
        files.split("\r\n").eachWithIndex { fileName, i ->
            try {
                log.info("opening file $i, name: $fileName")
                file = new File(fileName)
                file.eachLine { line ->
                    if (line.contains(pattern)) {
                        String message = line.split("\\{")[1].split("}")[0]
                        log.info("{$message}")
                        messages.add("{$message}")
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
}
