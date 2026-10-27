package com.uco.ucopetpaymentprocessor.consumer.invoice;

import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCancelMessageDTO;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCreateMessageDTO;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoicePaymentMessageDTO;
import com.uco.ucopetpaymentprocessor.json.JsonObjectMapper;
import com.uco.ucopetpaymentprocessor.service.invoice.InvoiceService;
import com.uco.ucopetpaymentprocessor.service.invoice.exception.InvoiceProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMessageListener {

    private static final Logger log = LoggerFactory.getLogger(InvoiceMessageListener.class);

    private final InvoiceService invoiceService;
    private final JsonObjectMapper jsonObjectMapper;

    public InvoiceMessageListener(final InvoiceService invoiceService,
                                  final JsonObjectMapper jsonObjectMapper) {
        this.invoiceService = invoiceService;
        this.jsonObjectMapper = jsonObjectMapper;
    }

    @RabbitListener(queues = "${ucopet.payment.invoice.create.queue}")
    public void onCreate(final String body,
                         @Header(value = AmqpHeaders.MESSAGE_ID, required = false) final String messageId) {
        process("create", messageId,
                () -> invoiceService.registerNewInvoice(jsonObjectMapper.fromJson(body, InvoiceCreateMessageDTO.class)));
    }

    @RabbitListener(queues = "${ucopet.payment.invoice.payment.queue}")
    public void onPayment(final String body,
                          @Header(value = AmqpHeaders.MESSAGE_ID, required = false) final String messageId) {
        process("payment", messageId,
                () -> invoiceService.registerPayment(jsonObjectMapper.fromJson(body, InvoicePaymentMessageDTO.class)));
    }

    @RabbitListener(queues = "${ucopet.payment.invoice.cancel.queue}")
    public void onCancel(final String body,
                         @Header(value = AmqpHeaders.MESSAGE_ID, required = false) final String messageId) {
        process("cancel", messageId,
                () -> invoiceService.cancelInvoice(jsonObjectMapper.fromJson(body, InvoiceCancelMessageDTO.class)));
    }

    private void process(final String operation, final String messageId, final Runnable action) {
        log.info("Factura [{}] recibida, messageId={}", operation, messageId);
        try {
            action.run();
            log.info("Factura [{}] procesada, messageId={}", operation, messageId);
        } catch (InvoiceProcessingException e) {
            log.warn("Factura [{}] rechazada por regla de negocio, messageId={}: {}", operation, messageId, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Factura [{}] falló, messageId={}: {}", operation, messageId, e.getMessage(), e);
            throw e;
        }
    }
}