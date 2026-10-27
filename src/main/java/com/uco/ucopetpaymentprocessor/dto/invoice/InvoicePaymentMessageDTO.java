package com.uco.ucopetpaymentprocessor.dto.invoice;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoicePaymentMessageDTO(UUID invoiceId, BigDecimal amount) {
}