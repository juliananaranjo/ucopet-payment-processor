package com.uco.ucopetpaymentprocessor.service.receipts;

import com.uco.ucopetpaymentprocessor.domain.payMethods.PayMethodDomain;
import com.uco.ucopetpaymentprocessor.domain.receipts.ReceiptDomain;
import com.uco.ucopetpaymentprocessor.dto.receipt.ReceiptStatus;
import com.uco.ucopetpaymentprocessor.dto.receipt.ReceiptCancelMessageDTO;
import com.uco.ucopetpaymentprocessor.dto.receipt.ReceiptMessageDTO;
import com.uco.ucopetpaymentprocessor.repository.payMethods.PayMethodRepository;
import com.uco.ucopetpaymentprocessor.repository.receipts.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);

    private final ReceiptRepository receiptRepository;
    private final PayMethodRepository payMethodRepository;

    public ReceiptService(final ReceiptRepository receiptRepository,
                          final PayMethodRepository payMethodRepository) {
        this.receiptRepository = receiptRepository;
        this.payMethodRepository = payMethodRepository;
    }

    @Transactional
    public void create(final ReceiptMessageDTO message) {
        validate(message);
        if (receiptRepository.existsById(message.receiptId())) {
            log.warn("Recibo {} ya existe, se ignora el mensaje duplicado", message.receiptId());
            return;
        }

        final ReceiptDomain receipt = ReceiptDomain.builder()
                .id(message.receiptId())
                .receiptNumber(nextReceiptNumber())
                .tutorId(message.tutorId())
                .petId(message.petId())
                .concept(message.concept())
                .amount(message.amount())
                .payMethod(resolvePayMethod(message))
                .date(message.date())
                .state(ReceiptStatus.ACTIVE)
                .build();

        receiptRepository.save(receipt);
        log.info("Recibo {} creado con número {}", receipt.getId(), receipt.getReceiptNumber());
    }

    @Transactional
    public void update(final ReceiptMessageDTO message) {
        validate(message);

        final ReceiptDomain receipt = receiptRepository.findById(message.receiptId())
                .orElseThrow(() -> new IllegalStateException("El recibo no existe: " + message.receiptId()));

        if (receipt.getState() == ReceiptStatus.CANCELLED) {
            throw new IllegalStateException("No se puede modificar un recibo anulado: " + message.receiptId());
        }

        receipt.setTutorId(message.tutorId());
        receipt.setPetId(message.petId());
        receipt.setConcept(message.concept());
        receipt.setAmount(message.amount());
        receipt.setPayMethod(resolvePayMethod(message));
        receipt.setDate(message.date());

        receiptRepository.save(receipt);
        log.info("Recibo {} actualizado", receipt.getId());
    }

    @Transactional
    public void cancel(final ReceiptCancelMessageDTO message) {
        if (message == null || message.receiptId() == null) {
            throw new IllegalArgumentException("El mensaje de anulación no trae receiptId");
        }

        final ReceiptDomain receipt = receiptRepository.findById(message.receiptId())
                .orElseThrow(() -> new IllegalStateException("El recibo no existe: " + message.receiptId()));

        if (receipt.getState() == ReceiptStatus.CANCELLED) {
            log.warn("Recibo {} ya estaba anulado, se ignora el mensaje", receipt.getId());
            return;
        }

        receipt.setState(ReceiptStatus.CANCELLED);
        receiptRepository.save(receipt);
        log.info("Recibo {} anulado", receipt.getId());
    }

    private PayMethodDomain resolvePayMethod(final ReceiptMessageDTO message) {
        return payMethodRepository.findById(message.payMethodId())
                .orElseThrow(() -> new IllegalStateException(
                        "El método de pago no existe: " + message.payMethodId()));
    }

    private void validate(final ReceiptMessageDTO message) {
        if (message == null || message.receiptId() == null) {
            throw new IllegalArgumentException("El mensaje no trae receiptId");
        }
        if (message.tutorId() == null) {
            throw new IllegalArgumentException("El tutor es obligatorio");
        }
        if (message.concept() == null || message.concept().isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }
        if (message.amount() == null || message.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor a cero");
        }
        if (message.payMethodId() == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }
        if (message.date() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
    }

    private String nextReceiptNumber() {
        return String.format("REC-%06d", receiptRepository.count() + 1);
    }
}
