package com.identifix.rabbitmqmessagessenderservice.controller

import com.identifix.rabbitmqmessagessenderservice.proposal.FilesProcessor
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
@Component
@Slf4j
@RequestMapping("/file")
class FilesController {

    @Autowired
    FilesProcessor filesProcessor

    @PostMapping()
    ResponseEntity<String> sendMessages(@RequestBody String fileName) {
        File file = new File(fileName)
        String response = file.getBytes().encodeBase64().toString()
        new ResponseEntity(response, HttpStatus.ACCEPTED)
    }

    @PostMapping("/notFoundToc")
    ResponseEntity<String> readMessages(@RequestBody String filePaths) {
        String response = filesProcessor.generateToc(filePaths)
        new ResponseEntity(response, HttpStatus.ACCEPTED)
    }
}
