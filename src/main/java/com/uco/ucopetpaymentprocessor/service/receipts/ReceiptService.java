package com.uco.ucopetpaymentprocessor.service.receipts;

import com.uco.ucopetpaymentprocessor.domain.payMethods.PayMethodDomain;
import com.uco.ucopetpaymentprocessor.domain.receipts.ReceiptDomain;
import com.uco.ucopetpaymentprocessor.domain.receipts.receiptStatus.ReceiptStatus;
import com.uco.ucopetpaymentprocessor.repository.paymethod.PayMethodRepository;
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
    public void create(final ReceiptDomain incoming) {
        validate(incoming);

        // Idempotencia: si el mensaje se reentrega, no se duplica el recibo.
        if (receiptRepository.existsById(incoming.getId())) {
            log.warn("Recibo {} ya existe, se ignora el mensaje duplicado", incoming.getId());
            return;
        }

        final ReceiptDomain receipt = ReceiptDomain.builder()
                .id(incoming.getId())
                .receiptNumber(nextReceiptNumber())      // lo asigna el processor
                .tutorId(incoming.getTutorId())
                .petId(incoming.getPetId())
                .concept(incoming.getConcept())
                .amount(incoming.getAmount())
                .payMethod(resolvePayMethod(incoming))
                .date(incoming.getDate())
                .state(ReceiptStatus.ACTIVE)             // lo decide el processor
                .build();

        receiptRepository.save(receipt);
        log.info("Recibo {} creado con número {}", receipt.getId(), receipt.getReceiptNumber());
    }

    @Transactional
    public void update(final ReceiptDomain incoming) {
        validate(incoming);

        final ReceiptDomain receipt = receiptRepository.findById(incoming.getId())
                .orElseThrow(() -> new IllegalStateException("El recibo no existe: " + incoming.getId()));

        if (receipt.getState() == ReceiptStatus.CANCELLED) {
            throw new IllegalStateException("No se puede modificar un recibo anulado: " + incoming.getId());
        }

        receipt.setTutorId(incoming.getTutorId());
        receipt.setPetId(incoming.getPetId());
        receipt.setConcept(incoming.getConcept());
        receipt.setAmount(incoming.getAmount());
        receipt.setPayMethod(resolvePayMethod(incoming));
        receipt.setDate(incoming.getDate());

        receiptRepository.save(receipt);
        log.info("Recibo {} actualizado", receipt.getId());
    }

    @Transactional
    public void cancel(final ReceiptDomain incoming) {
        if (incoming == null || incoming.getId() == null) {
            throw new IllegalArgumentException("El mensaje de anulación no trae id");
        }

        final ReceiptDomain receipt = receiptRepository.findById(incoming.getId())
                .orElseThrow(() -> new IllegalStateException("El recibo no existe: " + incoming.getId()));

        if (receipt.getState() == ReceiptStatus.CANCELLED) {
            log.warn("Recibo {} ya estaba anulado, se ignora el mensaje", receipt.getId());
            return;
        }

        receipt.setState(ReceiptStatus.CANCELLED);
        receiptRepository.save(receipt);
        log.info("Recibo {} anulado", receipt.getId());
    }

    private PayMethodDomain resolvePayMethod(final ReceiptDomain incoming) {
        return payMethodRepository.findById(incoming.getPayMethod().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "El método de pago no existe: " + incoming.getPayMethod().getId()));
    }

    private void validate(final ReceiptDomain incoming) {
        if (incoming == null || incoming.getId() == null) {
            throw new IllegalArgumentException("El mensaje no trae id");
        }
        if (incoming.getTutorId() == null) {
            throw new IllegalArgumentException("El tutor es obligatorio");
        }
        if (incoming.getConcept() == null || incoming.getConcept().isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }
        if (incoming.getAmount() == null || incoming.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor a cero");
        }
        if (incoming.getPayMethod() == null || incoming.getPayMethod().getId() == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }
        if (incoming.getDate() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
    }

    private String nextReceiptNumber() {
        return String.format("REC-%06d", receiptRepository.count() + 1);
    }
}
