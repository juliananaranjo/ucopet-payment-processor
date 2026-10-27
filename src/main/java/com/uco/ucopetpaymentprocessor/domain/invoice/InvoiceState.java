package com.uco.ucopetpaymentprocessor.domain.invoice;

/**
 * Copia exacta del enum de la API: los valores se guardan como texto en la
 * columna "state", asi que deben escribirse igual en los dos proyectos.
 */
public enum InvoiceState { PENDIENTE, PARCIALMENTE_PAGADA, PAGADA, ANULADA, SIN_ESTADO }