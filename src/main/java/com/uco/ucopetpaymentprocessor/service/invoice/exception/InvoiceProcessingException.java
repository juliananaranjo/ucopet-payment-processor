package com.uco.ucopetpaymentprocessor.service.invoice.exception;

/**
 * Se lanza cuando un mensaje de facturas incumple una regla de negocio
 * (orden inexistente, factura ya anulada, pago mayor al saldo, etc.).
 * El consumidor la registra en el log y descarta el mensaje.
 */
public class InvoiceProcessingException extends RuntimeException {

    public InvoiceProcessingException(String message) {
        super(message);
    }
}