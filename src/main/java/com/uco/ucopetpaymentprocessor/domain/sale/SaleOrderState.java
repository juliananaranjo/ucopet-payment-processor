package com.uco.ucopetpaymentprocessor.domain.sale;

/**
 * Copia exacta del enum de la API: los valores se guardan como texto en la
 * columna "state" de la orden de venta.
 */
public enum SaleOrderState { BORRADOR, FACTURADA, ANULADA, SIN_ESTADO }