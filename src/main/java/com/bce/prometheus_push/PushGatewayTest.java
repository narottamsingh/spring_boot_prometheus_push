package com.bce.prometheus_push;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;

public class PushGatewayTest {

  public static void main(String[] args) throws Exception {
    CollectorRegistry registry = new CollectorRegistry();
    Gauge gauge = Gauge.build()
            .name("testing_metric")
            .help("A testing metric")
            .register(registry);

    gauge.set(42); // Example metric value

    PushGateway pushGateway = new PushGateway("localhost:9091");
    pushGateway.pushAdd(registry, "debug_job");
    System.out.println("Test metric pushed successfully!");
  }
}