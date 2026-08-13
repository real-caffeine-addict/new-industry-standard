package com.psw.gateway;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingConfigurationIntegrationTests {

    @Test
    void dedicatedTechnicalLoggersAreNonAdditiveAndEmitOneEvent() throws Exception {
        LoggerContext context = new LoggerContext();
        URL configuration = getClass().getClassLoader().getResource("logback-spring.xml");
        assertThat(configuration).isNotNull();
        new JoranConfigurator().setContext(context);
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(configuration);

        Logger requestLogger = context.getLogger("logRequest");
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> dedicated = new ListAppender<>();
        ListAppender<ILoggingEvent> root = new ListAppender<>();
        dedicated.setContext(context);
        root.setContext(context);
        dedicated.start();
        root.start();
        requestLogger.addAppender(dedicated);
        rootLogger.addAppender(root);

        requestLogger.info("request-log");

        assertThat(requestLogger.isAdditive()).isFalse();
        assertThat(dedicated.list).hasSize(1);
        assertThat(root.list).isEmpty();
        assertThat(requestLogger.getAppender("JSON_LOG")).isNotNull();
        context.stop();
    }
}
