package com.example.demo.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Lee los nombres desde application.properties (prefijo app.rabbit). */
@ConfigurationProperties(prefix = "app.rabbit")
public record MensajeriaProperties(
        String exchange,
        String dlx,
        Routing routing,
        Cola notificaciones,
        Cola tickets) {

    public record Routing(String pedidoCreado, String pedidoEstado) {}

    public record Cola(String queue, String dlq, String bindingKey) {}
}