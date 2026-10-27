package com.uco.ucopetpaymentprocessor.config.invoice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ucopet.payment.invoice")
public record InvoiceQueueProperties(Route create, Route payment, Route cancel) {

    public record Route(String exchange, String queue, String routingKey) {
    }
}