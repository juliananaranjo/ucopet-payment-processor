package com.uco.ucopetpaymentprocessor.dto.invoice;

import java.util.UUID;

public record InvoiceCreateMessageDTO(UUID invoiceId, UUID saleOrderId, String observations) {
}