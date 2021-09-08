package com.identifix.rabbitmqmessagessenderservice

import com.identifix.rabbitmqmessagessenderservice.proposal.MessageSender
import com.identifix.rabbitmqmessagessenderservice.service.UtilityService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
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
    @Autowired
    UtilityService utilityService

    @PostMapping("/sender")
    ResponseEntity<String> sendMessages(@RequestParam String exchange, @RequestBody String files) {
        String response = messageSender.processFile(exchange,files)
        new ResponseEntity(response, HttpStatus.ACCEPTED)
    }

    @GetMapping("/dateValidation/pages")
    ResponseEntity<String> validatePagesDate(@RequestParam String manualUid, @RequestParam String limitDate) {
        String response = utilityService.validateManualPagesDate(manualUid,limitDate)
        new ResponseEntity(response, HttpStatus.ACCEPTED)
    }
}
