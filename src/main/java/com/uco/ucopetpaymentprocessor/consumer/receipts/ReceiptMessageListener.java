package com.uco.ucopetpaymentprocessor.consumer.receipts;

import com.uco.ucopetpaymentprocessor.domain.receipts.ReceiptDomain;
import com.uco.ucopetpaymentprocessor.json.JsonObjectMapper;
import com.uco.ucopetpaymentprocessor.service.receipts.ReceiptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class ReceiptMessageListener {

    private static final Logger log = LoggerFactory.getLogger(ReceiptMessageListener.class);

    private final ReceiptService receiptService;
    private final JsonObjectMapper jsonObjectMapper;

    public ReceiptMessageListener(final ReceiptService receiptService,
                                  final JsonObjectMapper jsonObjectMapper) {
        this.receiptService = receiptService;
        this.jsonObjectMapper = jsonObjectMapper;
    }

    @RabbitListener(queues = "${ucopet.payment.receipt.create.queue}")
    public void onCreate(final String body,
                         @Header(value = AmqpHeaders.MESSAGE_ID, required = false) final String messageId) {
        process("create", messageId,
                () -> receiptService.create(jsonObjectMapper.fromJson(body, ReceiptDomain.class)));
    }

    @RabbitListener(queues = "${ucopet.payment.receipt.update.queue}")
    public void onUpdate(final String body,
                         @Header(value = AmqpHeaders.MESSAGE_ID, required = false) final String messageId) {
        process("update", messageId,
                () -> receiptService.update(jsonObjectMapper.fromJson(body, ReceiptDomain.class)));
    }

    @RabbitListener(queues = "${ucopet.payment.receipt.cancel.queue}")
    public void onCancel(final String body,
                         @Header(value = AmqpHeaders.MESSAGE_ID, required = false) final String messageId) {
        process("cancel", messageId,
                () -> receiptService.cancel(jsonObjectMapper.fromJson(body, ReceiptDomain.class)));
    }
    private void process(final String operation, final String messageId, final Runnable action) {
        log.info("Recibo [{}] recibido, messageId={}", operation, messageId);
        try {
            action.run();
        } catch (RuntimeException e) {
            log.error("Recibo [{}] falló, messageId={}: {}", operation, messageId, e.getMessage(), e);
            throw e;
        }
    }
}
