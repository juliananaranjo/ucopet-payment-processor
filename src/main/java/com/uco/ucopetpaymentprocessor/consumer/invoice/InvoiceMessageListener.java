package com.uco.ucopetpaymentprocessor.consumer.invoice;

import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCancelMessage;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCreateMessage;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoicePaymentMessage;
import com.uco.ucopetpaymentprocessor.json.JsonObjectMapper;
import com.uco.ucopetpaymentprocessor.service.invoice.InvoiceService;
import com.uco.ucopetpaymentprocessor.service.invoice.exception.InvoiceProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de las colas de facturas. No tiene logica de negocio: convierte
 * el JSON recibido en el mensaje correspondiente y se lo entrega al service.
 *
 * Si el service rechaza el mensaje por una regla de negocio, se registra en el
 * log y el mensaje se descarta. Cualquier otro error (por ejemplo, un JSON mal
 * formado) lo registra Spring, y el mensaje tambien se descarta gracias a
 * default-requeue-rejected=false en payments.properties.
 */
@Component
public class InvoiceMessageListener {

    private static final Logger log = LoggerFactory.getLogger(InvoiceMessageListener.class);

    private final JsonObjectMapper jsonObjectMapper;
    private final InvoiceService invoiceService;

    public InvoiceMessageListener(JsonObjectMapper jsonObjectMapper, InvoiceService invoiceService) {
        this.jsonObjectMapper = jsonObjectMapper;
        this.invoiceService = invoiceService;
    }

    @RabbitListener(queues = "${ucopet.payment.bindings.invoice-create.queue}")
    public void listenCreate(String message) {
        process("invoice-create", message,
                () -> invoiceService.registerNewInvoice(jsonObjectMapper.fromJson(message, InvoiceCreateMessage.class)));
    }

    @RabbitListener(queues = "${ucopet.payment.bindings.invoice-payment.queue}")
    public void listenPayment(String message) {
        process("invoice-payment", message,
                () -> invoiceService.registerPayment(jsonObjectMapper.fromJson(message, InvoicePaymentMessage.class)));
    }

    @RabbitListener(queues = "${ucopet.payment.bindings.invoice-cancel.queue}")
    public void listenCancel(String message) {
        process("invoice-cancel", message,
                () -> invoiceService.cancelInvoice(jsonObjectMapper.fromJson(message, InvoiceCancelMessage.class)));
    }

    private void process(String operation, String message, Runnable action) {
        log.info("Mensaje recibido en {}: {}", operation, message);
        try {
            action.run();
            log.info("Mensaje de {} procesado correctamente.", operation);
        } catch (InvoiceProcessingException ex) {
            log.warn("Mensaje de {} rechazado: {}", operation, ex.getMessage());
        }
    }
}