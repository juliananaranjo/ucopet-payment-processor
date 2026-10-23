package com.uco.ucopetpaymentprocessor.dto.invoice;

import java.util.UUID;

/**
 * Mensaje de la cola invoice-create. Debe tener los mismos campos que el
 * record del mismo nombre en la API, porque viaja como JSON entre los dos.
 */
public record InvoiceCreateMessage(UUID invoiceId, UUID saleOrderId, String observations) {
}