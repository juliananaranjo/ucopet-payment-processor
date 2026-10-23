package com.uco.ucopetpaymentprocessor.service.egress;

import com.uco.ucopetpaymentprocessor.domain.egress.EgressDomain;
import com.uco.ucopetpaymentprocessor.repository.egress.EgressRepository;
import com.uco.ucopetpaymentprocessor.repository.paymethod.PayMethodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
public class EgressService {

    private static final Logger log = LoggerFactory.getLogger(EgressService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");

    private final EgressRepository egressRepository;
    private final PayMethodRepository payMethodRepository;

    public EgressService(final EgressRepository egressRepository,
                         final PayMethodRepository payMethodRepository) {
        this.egressRepository = egressRepository;
        this.payMethodRepository = payMethodRepository;
    }

    // ---------- Crear / actualizar ----------

    @Transactional
    public void saveEgress(final EgressDomain message) {
        if (message == null || message.getId() == null) {
            throw new IllegalArgumentException("El mensaje no trae el id del egreso");
        }
        validateEgress(message);

        final Optional<EgressDomain> existing = egressRepository.findById(message.getId());
        if (existing.isPresent()) {
            updateEgress(existing.get(), message);
        } else {
            createEgress(message);
        }
    }

    private void createEgress(final EgressDomain message) {
        message.setConcept(message.getConcept().trim());
        egressRepository.saveAndFlush(message);
        log.info("[EGRESS] creado id={}", message.getId());
    }

    private void updateEgress(final EgressDomain egress, final EgressDomain changes) {
        egress.setDate(changes.getDate());
        egress.setProvider(changes.getProvider());
        egress.setPayMethod(changes.getPayMethod());
        egress.setPurchaseOrder(changes.getPurchaseOrder());
        egress.setConcept(changes.getConcept().trim());
        egress.setTotal(changes.getTotal());
        egressRepository.saveAndFlush(egress);
        log.info("[EGRESS] actualizado id={}", egress.getId());
    }

    // ---------- Eliminar ----------

    @Transactional
    public void deleteEgress(final UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El mensaje no trae el id del egreso");
        }
        if (!egressRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe un egreso con id: " + id);
        }
        egressRepository.deleteById(id);
        log.info("[EGRESS] eliminado id={}", id);
    }

    // ---------- Validaciones (segunda revisión) ----------

    private void validateEgress(final EgressDomain egress) {
        validateDate(egress.getDate());
        validateRequired(egress.getProvider(), "El proveedor es obligatorio");
        validateRequired(egress.getPurchaseOrder(), "La orden de compra es obligatoria");
        validatePayMethod(egress.getPayMethod());
        validateConcept(egress.getConcept());
        validateTotal(egress.getTotal());
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (date.isAfter(LocalDate.now(ZONE_ID))) {
            throw new IllegalArgumentException("La fecha no puede ser posterior a la fecha actual");
        }
    }

    private void validateRequired(final UUID value, final String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validatePayMethod(final UUID payMethodId) {
        validateRequired(payMethodId, "El método de pago es obligatorio");
        if (!payMethodRepository.existsById(payMethodId)) {
            throw new IllegalArgumentException("No existe un método de pago con id: " + payMethodId);
        }
    }

    private void validateConcept(final String concept) {
        if (concept == null || concept.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }
    }

    private void validateTotal(final Float total) {
        if (total == null) {
            throw new IllegalArgumentException("El total es obligatorio");
        }
        if (total <= 0) {
            throw new IllegalArgumentException("El total no puede ser negativo ni cero");
        }
    }
}