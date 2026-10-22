package com.uco.ucopetpaymentprocessor.config.receipts;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ucopet.payment.receipt")
public record ReceiptQueueProperties(Route create, Route update, Route cancel) {

    public record Route(String exchange, String queue, String routingKey) {
    }
}
