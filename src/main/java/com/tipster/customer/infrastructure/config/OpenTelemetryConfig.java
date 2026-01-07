package com.tipster.customer.infrastructure.config;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenTelemetryConfig {

    @Value("${otel.exporter.otlp.endpoint}")
    private String otlpBaseEndpoint;

    @Value("${otel.service.name}")
    private String serviceName;

    @Value("${otel.resource.attributes.deployment.environment}")
    private String environment;

    public static SdkLoggerProvider loggerProvider;

    @PostConstruct
    public void setupOpenTelemetry() {
        Resource resource = Resource.create(
                Attributes.builder()
                        .put("service.name", serviceName)
                        .put("deployment.environment", environment)
                        .build()
        );
        SpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(otlpBaseEndpoint + "/v1/traces")
                .setTimeout(Duration.ofSeconds(10))
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();
        MetricExporter metricExporter = OtlpHttpMetricExporter.builder()
                .setEndpoint(otlpBaseEndpoint + "/v1/metrics")
                .build();

        MetricReader metricReader = PeriodicMetricReader.builder(metricExporter)
                .setInterval(Duration.ofSeconds(30))
                .build();

        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(metricReader)
                .build();
        LogRecordExporter logExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint(otlpBaseEndpoint + "/v1/logs")
                .build();

        loggerProvider = SdkLoggerProvider.builder()
                .setResource(resource)
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
                .build();
        OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
                .setLoggerProvider(loggerProvider)
                .buildAndRegisterGlobal();
        Logger otelLogger = loggerProvider.get("logback-otel");

        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        OpenTelemetryLoggerAppender otelAppender = new OpenTelemetryLoggerAppender();
        otelAppender.setOtelLogger(otelLogger);
        otelAppender.setContext(rootLogger.getLoggerContext());
        otelAppender.start();

        rootLogger.addAppender(otelAppender);
    }
}
