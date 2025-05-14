package com.bce.prometheus_push;

import io.prometheus.client.exporter.PushGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrometheusPushGatewayConfig {

  @Value("${management.metrics.export.pushgateway.base-url}")
  private String host;

  @Bean
  public PushGateway pushGateway() {
    return new PushGateway(host); // Use the PushGateway endpoint
  }
}