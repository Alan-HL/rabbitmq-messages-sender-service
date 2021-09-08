package com.identifix.rabbitmqmessagessenderservice.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class UtilityServiceConfig {
    @Value('${content-labeling-service.kraken-uri}')
    String krakenUri

    @Value('${content-labeling-service.security-service-uri}')
    String securityServiceUri

    @Value('${client_id}')
    String clientId

    @Value('${client_secret}')
    String clientSecret
}
