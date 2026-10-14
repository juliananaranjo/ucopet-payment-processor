package com.uco.ucopetpaymentprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Lee de payments.properties el exchange y, por cada operacion
 * (invoice-create, receipt-update, ...), su cola y su routing key.
 */
@ConfigurationProperties(prefix = "ucopet.payment")
public record PaymentQueueProperties(String exchange, Map<String, Route> bindings) {

    public record Route(String queue, String routingKey) {
    }
}