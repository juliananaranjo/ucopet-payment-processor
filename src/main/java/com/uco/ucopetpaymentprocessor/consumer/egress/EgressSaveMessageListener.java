package com.uco.ucopetpaymentprocessor.consumer.egress;

import com.uco.ucopetpaymentprocessor.domain.egress.EgressDomain;
import com.uco.ucopetpaymentprocessor.json.JsonObjectMapper;
import com.uco.ucopetpaymentprocessor.service.egress.EgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class EgressSaveMessageListener {

    private static final Logger log = LoggerFactory.getLogger(EgressSaveMessageListener.class);

    private final EgressService egressService;
    private final JsonObjectMapper jsonObjectMapper;

    public EgressSaveMessageListener(final EgressService egressService,
                                     final JsonObjectMapper jsonObjectMapper) {
        this.egressService = egressService;
        this.jsonObjectMapper = jsonObjectMapper;
    }

    @RabbitListener(queues = "${payments.egress.save.queue-name}")
    public void receiveMessage(final Message message) {
        final Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        final String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[EGRESS-SAVE] recibido idMensaje={}", idMessage);

        final EgressDomain egress = toEgress(json, idMessage);

        try {
            egressService.saveEgress(egress);
        } catch (IllegalArgumentException e) {
            log.warn("[EGRESS-SAVE] rechazado idMensaje={}: {}", idMessage, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

    private EgressDomain toEgress(final String json, final Object idMessage) {
        try {
            return jsonObjectMapper.fromJson(json, EgressDomain.class);
        } catch (RuntimeException e) {
            log.warn("[EGRESS-SAVE] JSON inválido idMensaje={}: {}", idMessage, json);
            throw new AmqpRejectAndDontRequeueException("El mensaje no es el JSON de un egreso", e);
        }
    }
}