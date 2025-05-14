package com.bce.prometheus_push;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

@SpringBootApplication
public class PrometheusPushApplication implements CommandLineRunner {

  @Autowired
  private MeterRegistry meterRegistry; // Used for Micrometer-based metrics

  @Autowired
  private PushGateway pushGateway; // PushGateway instance for Prometheus

  public static void main(String[] args) {
    SpringApplication.run(PrometheusPushApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    // Create Prometheus CollectorRegistry (holds custom Prometheus metrics)
    for (var i = 0; i < 10; i++) {
      CollectorRegistry collectorRegistry = new CollectorRegistry();

      // Track and increment app run counter
      Counter runCounter = Counter.build()
              .name("application_run_count")
              .help("Counts how many times the application has run")
              .register(collectorRegistry);

      runCounter.inc(); // Increment the run counter

      // Measure task processing time
      Timer.Sample timerSample = Timer.start(meterRegistry);

      System.out.println("Processing task...");
      Thread.sleep(5000); // Simulating a task
      System.out.println("Task finished!");

      double processingTime = timerSample.stop(meterRegistry.timer("processing_duration_seconds"));

      // Add resource (CPU/memory) usage as metrics
      registerSystemMetrics(collectorRegistry);

      // Push all metrics to PushGateway
      pushGateway.pushAdd(collectorRegistry, "my-short-lived-app");
    }
    System.exit(0); // Exit application after processing (short-lived app)
  }

  private void registerSystemMetrics(CollectorRegistry collectorRegistry) {
    // Get JVM and system runtime information
    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    // Track heap memory used (in bytes)
    Gauge memoryUsedGauge = Gauge.build()
            .name("jvm_memory_used_bytes")
            .help("Heap memory currently used (in bytes)")
            .register(collectorRegistry);

    memoryUsedGauge.set(memoryMXBean.getHeapMemoryUsage().getUsed());

    // Track max heap memory (in bytes)
    Gauge memoryMaxGauge = Gauge.build()
            .name("jvm_memory_max_bytes")
            .help("Maximum heap memory (in bytes)")
            .register(collectorRegistry);

    memoryMaxGauge.set(memoryMXBean.getHeapMemoryUsage().getMax());

    // Track CPU usage (system-wide load average)
    Gauge cpuLoadGauge = Gauge.build()
            .name("system_cpu_load")
            .help("System-wide CPU load (averaged across all processors)")
            .register(collectorRegistry);

    cpuLoadGauge.set(osMXBean.getSystemLoadAverage());

    // Track JVM uptime (in seconds)
    Gauge processUptimeGauge = Gauge.build()
            .name("process_uptime_seconds")
            .help("JVM process uptime (in seconds)")
            .register(collectorRegistry);

    processUptimeGauge.set(runtimeMXBean.getUptime() / 1000.0); // Convert from ms to seconds
  }
}