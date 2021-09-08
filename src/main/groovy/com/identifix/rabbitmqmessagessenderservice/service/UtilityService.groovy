package com.identifix.rabbitmqmessagessenderservice.service

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Component
@Slf4j
class UtilityService {
    String validateManualPagesDate(String manualUid, String limitDate) {
        "TEST" + manualUid + limitDate
    }
}
