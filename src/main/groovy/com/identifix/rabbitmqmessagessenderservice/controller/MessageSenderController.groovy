package com.identifix.rabbitmqmessagessenderservice

import com.identifix.rabbitmqmessagessenderservice.proposal.MessageSender
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
@Component
@Slf4j
@RequestMapping("/run")
class MessageSenderController {
    @Autowired
    MessageSender messageSender


    @PostMapping("/sender")
    ResponseEntity<String> sendMessages(@RequestParam String exchange, @RequestBody String files) {
        String response = messageSender.processFile(exchange,files)
        new ResponseEntity(response, HttpStatus.ACCEPTED)
    }

    @PostMapping("/reader")
    ResponseEntity<String> readMessages(@RequestParam String queueName, @RequestBody String filePath) {
        String response = messageSender.processQueue(queueName, filePath)
        new ResponseEntity(response, HttpStatus.ACCEPTED)
    }

    @PostMapping("/readerAndSender")
    ResponseEntity<String> readAndSendMessages(@RequestParam String exchange, @RequestBody String filePath) {
        String response = messageSender.processFiles(exchange, filePath)
        new ResponseEntity(response, HttpStatus.ACCEPTED)
    }
}
