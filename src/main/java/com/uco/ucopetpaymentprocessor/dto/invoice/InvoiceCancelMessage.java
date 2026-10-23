package com.uco.ucopetpaymentprocessor.dto.invoice;

import java.util.UUID;

/**
 * Mensaje de la cola invoice-cancel. Mismos campos que en la API.
 */
public record InvoiceCancelMessage(UUID invoiceId) {
}