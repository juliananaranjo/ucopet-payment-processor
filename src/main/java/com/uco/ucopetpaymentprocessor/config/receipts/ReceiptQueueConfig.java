package com.uco.ucopetpaymentprocessor.config.receipts;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(ReceiptQueueProperties.class)
public class ReceiptQueueConfig {

    @Bean
    public Declarables receiptDeclarables(final ReceiptQueueProperties properties) {
        final List<Declarable> declarables = new ArrayList<>();

        for (ReceiptQueueProperties.Route route : List.of(properties.create(), properties.update(), properties.cancel())) {
            final DirectExchange exchange = new DirectExchange(route.exchange(), true, false);
            final Queue queue = QueueBuilder.durable(route.queue()).build();
            declarables.add(exchange);
            declarables.add(queue);
            declarables.add(BindingBuilder.bind(queue).to(exchange).with(route.routingKey()));
        }
        return new Declarables(declarables);
    }
}
