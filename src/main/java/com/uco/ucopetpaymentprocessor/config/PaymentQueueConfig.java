package com.uco.ucopetpaymentprocessor.config;

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

/**
 * Declara en RabbitMQ el exchange de pagos y, por cada entrada de
 * payments.properties, una cola durable enlazada con su routing key.
 * Agregar una operacion nueva solo requiere agregar sus dos lineas al properties.
 */
@Configuration
@EnableConfigurationProperties(PaymentQueueProperties.class)
public class PaymentQueueConfig {

    @Bean
    public Declarables paymentDeclarables(PaymentQueueProperties properties) {
        DirectExchange exchange = new DirectExchange(properties.exchange(), true, false);

        List<Declarable> declarables = new ArrayList<>();
        declarables.add(exchange);

        properties.bindings().values().forEach(route -> {
            Queue queue = QueueBuilder.durable(route.queue()).build();
            declarables.add(queue);
            declarables.add(BindingBuilder.bind(queue).to(exchange).with(route.routingKey()));
        });

        return new Declarables(declarables);
    }
}