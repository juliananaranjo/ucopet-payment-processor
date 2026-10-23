package com.uco.ucopetpaymentprocessor.dto.invoice;

import java.util.UUID;

/**
 * Mensaje de la cola invoice-payment. Mismos campos que en la API.
 */
public record InvoicePaymentMessage(UUID invoiceId, Integer amount) {
}