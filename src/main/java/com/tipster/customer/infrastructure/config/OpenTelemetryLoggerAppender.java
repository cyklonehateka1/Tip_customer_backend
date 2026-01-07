package com.tipster.customer.infrastructure.config;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;

public class OpenTelemetryLoggerAppender extends AppenderBase<ILoggingEvent> {

    private Logger otelLogger;

    public void setOtelLogger(Logger otelLogger) {
        this.otelLogger = otelLogger;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (otelLogger == null) return;

        LogRecordBuilder builder = otelLogger.logRecordBuilder()
                .setBody(event.getFormattedMessage())
                .setSeverity(mapSeverity(event.getLevel().toString()))
                .setTimestamp(event.getTimeStamp(), java.util.concurrent.TimeUnit.MILLISECONDS);

        builder.emit();
    }

    private Severity mapSeverity(String level) {
        return switch (level) {
            case "ERROR" -> Severity.ERROR;
            case "WARN" -> Severity.WARN;
            case "INFO" -> Severity.INFO;
            case "DEBUG" -> Severity.DEBUG;
            case "TRACE" -> Severity.TRACE;
            default -> Severity.UNDEFINED_SEVERITY_NUMBER;
        };
    }
}
