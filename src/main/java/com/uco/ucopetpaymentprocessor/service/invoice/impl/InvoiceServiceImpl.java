package com.uco.ucopetpaymentprocessor.service.invoice.impl;

import com.uco.ucopetpaymentprocessor.domain.invoice.InvoiceDomain;
import com.uco.ucopetpaymentprocessor.domain.invoice.InvoiceState;
import com.uco.ucopetpaymentprocessor.domain.sale.SaleOrderDomain;
import com.uco.ucopetpaymentprocessor.domain.sale.SaleOrderState;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCancelMessage;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCreateMessage;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoicePaymentMessage;
import com.uco.ucopetpaymentprocessor.repository.invoice.InvoiceRepository;
import com.uco.ucopetpaymentprocessor.repository.sale.SaleOrderRepository;
import com.uco.ucopetpaymentprocessor.service.invoice.InvoiceService;
import com.uco.ucopetpaymentprocessor.service.invoice.exception.InvoiceProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Reglas de escritura de facturas, replicadas del InvoiceServiceImpl de la API.
 * La API ya valido la solicitud antes de publicarla, pero aqui se valida de
 * nuevo porque entre la publicacion y el procesamiento los datos pueden cambiar.
 */
@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private static final String INVOICE_NUMBER_PREFIX = "FV-";
    private static final int INVOICE_NUMBER_DIGITS = 5;
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final InvoiceRepository invoiceRepository;
    private final SaleOrderRepository saleOrderRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, SaleOrderRepository saleOrderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.saleOrderRepository = saleOrderRepository;
    }

    @Override
    @Transactional
    public void registerNewInvoice(InvoiceCreateMessage message) {
        requireId(message.invoiceId());

        // Si el mismo mensaje llega dos veces, la segunda no hace nada.
        if (invoiceRepository.existsById(message.invoiceId())) {
            log.info("La factura {} ya existe; se ignora el mensaje repetido.", message.invoiceId());
            return;
        }

        if (message.saleOrderId() == null) {
            throw new InvoiceProcessingException("saleOrderId es obligatorio.");
        }

        SaleOrderDomain saleOrder = saleOrderRepository.findById(message.saleOrderId())
                .orElseThrow(() -> new InvoiceProcessingException(
                        "No existe una orden de venta con id " + message.saleOrderId()));

        if (invoiceRepository.existsBySaleOrderId(saleOrder.getId())) {
            throw new InvoiceProcessingException(
                    "La orden de venta " + saleOrder.getOrderNumber() + " ya tiene una factura asociada.");
        }
        if (saleOrder.getState() != SaleOrderState.BORRADOR) {
            throw new InvoiceProcessingException(
                    "Solo se puede facturar una orden de venta en estado BORRADOR. Estado actual: "
                            + saleOrder.getState());
        }

        InvoiceDomain invoice = new InvoiceDomain();
        invoice.setId(message.invoiceId());
        invoice.setSaleOrderId(saleOrder.getId());
        invoice.setInvoiceNumber(generateNextInvoiceNumber(saleOrder.getHeadquarterId()));
        invoice.setHeadquarterId(saleOrder.getHeadquarterId());
        invoice.setClientID(saleOrder.getClientID());
        invoice.setPetId(saleOrder.getPetId());
        invoice.setIssueDate(LocalDateTime.now(BOGOTA));
        invoice.setTotal(saleOrder.getTotal());
        invoice.setPaidAmount(0);
        invoice.setBalance(saleOrder.getTotal());
        invoice.setObservations(message.observations());
        invoice.setState(InvoiceState.PENDIENTE);
        invoiceRepository.save(invoice);

        saleOrder.setState(SaleOrderState.FACTURADA);
        saleOrderRepository.save(saleOrder);
    }

    @Override
    @Transactional
    public void registerPayment(InvoicePaymentMessage message) {
        InvoiceDomain existing = findExistingOrThrow(message.invoiceId());

        if (existing.getState() == InvoiceState.ANULADA) {
            throw new InvoiceProcessingException("No se puede registrar un pago sobre una factura ANULADA.");
        }
        if (existing.getState() == InvoiceState.PAGADA) {
            throw new InvoiceProcessingException("La factura " + existing.getInvoiceNumber()
                    + " ya se encuentra completamente pagada.");
        }
        if (message.amount() == null || message.amount() <= 0) {
            throw new InvoiceProcessingException("amount debe ser mayor a cero.");
        }

        int newPaidAmount = existing.getPaidAmount() + message.amount();
        if (newPaidAmount > existing.getTotal()) {
            throw new InvoiceProcessingException(
                    "El pago excede el saldo pendiente. Saldo actual: " + existing.getBalance());
        }

        int newBalance = existing.getTotal() - newPaidAmount;
        existing.setPaidAmount(newPaidAmount);
        existing.setBalance(newBalance);
        existing.setState(newBalance == 0 ? InvoiceState.PAGADA : InvoiceState.PARCIALMENTE_PAGADA);
        invoiceRepository.save(existing);
    }

    @Override
    @Transactional
    public void cancelInvoice(InvoiceCancelMessage message) {
        InvoiceDomain existing = findExistingOrThrow(message.invoiceId());

        if (existing.getState() == InvoiceState.ANULADA) {
            throw new InvoiceProcessingException("La factura " + existing.getInvoiceNumber()
                    + " ya se encuentra ANULADA.");
        }

        existing.setState(InvoiceState.ANULADA);
        invoiceRepository.save(existing);
    }

    private void requireId(UUID invoiceId) {
        if (invoiceId == null) {
            throw new InvoiceProcessingException("invoiceId es obligatorio.");
        }
    }

    private InvoiceDomain findExistingOrThrow(UUID invoiceId) {
        requireId(invoiceId);
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceProcessingException("No existe una factura con id " + invoiceId));
    }

    private String generateNextInvoiceNumber(UUID headquarterId) {
        return invoiceRepository.findTopByHeadquarterIdOrderByInvoiceNumberDesc(headquarterId)
                .map(last -> increment(last.getInvoiceNumber()))
                .orElse(format(1));
    }

    private String increment(String lastInvoiceNumber) {
        String numericPart = lastInvoiceNumber.substring(INVOICE_NUMBER_PREFIX.length());
        return format(Integer.parseInt(numericPart) + 1);
    }

    private String format(int sequence) {
        return INVOICE_NUMBER_PREFIX + String.format("%0" + INVOICE_NUMBER_DIGITS + "d", sequence);
    }
}