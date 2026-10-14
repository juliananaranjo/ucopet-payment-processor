package com.uco.ucopetpaymentprocessor.consumer.invoice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de prueba: por ahora solo registra en consola lo que llega a la
 * cola de creacion de facturas. Sirve para comprobar que la conexion con
 * RabbitMQ funciona antes de implementar el service y el repository.
 */
@Component
public class InvoiceMessageListener {

    private static final Logger log = LoggerFactory.getLogger(InvoiceMessageListener.class);

    @RabbitListener(queues = "${ucopet.payment.bindings.invoice-create.queue}")
    public void listenCreate(String message) {
        log.info("Mensaje recibido en invoice-create: {}", message);
    }
}