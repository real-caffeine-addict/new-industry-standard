package com.psw.gateway.utility;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionParserUtilTests {
    private final ExceptionParserUtil parser = new ExceptionParserUtil();

    @Test
    void parsesNormalExceptionWithStackTrace() {
        Exception exception = new IllegalArgumentException("bad input");

        Map<String, Object> result = parser.exceptionParser(exception);

        assertThat(result).containsEntry("depth", 1)
                .containsEntry("truncated", false)
                .containsEntry("circularReference", false);
        assertThat(chain(result).getFirst())
                .containsEntry("type", "IllegalArgumentException")
                .containsEntry("message", "bad input")
                .containsEntry("stackTraceAvailable", true);
    }

    @Test
    void parsesNestedCausesInOrder() {
        Exception exception = new RuntimeException("outer",
                new IllegalStateException("middle", new Exception("inner")));

        Map<String, Object> result = parser.exceptionParser(exception);

        assertThat(chain(result)).extracting(item -> item.get("message"))
                .containsExactly("outer", "middle", "inner");
        assertThat(result).containsEntry("depth", 3).containsEntry("truncated", false);
    }

    @Test
    void truncatesCauseChainAfterFourEntries() {
        Exception exception = new Exception("five");
        exception = new Exception("four", exception);
        exception = new Exception("three", exception);
        exception = new Exception("two", exception);
        exception = new Exception("one", exception);

        Map<String, Object> result = parser.exceptionParser(exception);

        assertThat(chain(result)).hasSize(4);
        assertThat(result).containsEntry("depth", 4).containsEntry("truncated", true);
    }

    @Test
    void handlesEmptyStackTraceAndNullMessage() {
        Exception exception = new Exception((String) null);
        exception.setStackTrace(new StackTraceElement[0]);

        Map<String, Object> parsed = chain(parser.exceptionParser(exception)).getFirst();

        assertThat(parsed).containsEntry("message", null)
                .containsEntry("stackTraceAvailable", false)
                .doesNotContainKeys("sourceClass", "sourceMethod", "sourceFile", "lineNumber");
    }

    @Test
    void protectsAgainstCircularCauseGraph() {
        CircularException first = new CircularException("first");
        CircularException second = new CircularException("second");
        first.cause = second;
        second.cause = first;

        Map<String, Object> result = parser.exceptionParser(first);

        assertThat(chain(result)).hasSize(2);
        assertThat(result).containsEntry("circularReference", true)
                .containsEntry("truncated", false);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> chain(Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get("chain");
    }

    private static final class CircularException extends Exception {
        private Throwable cause;

        private CircularException(String message) {
            super(message, null);
        }

        @Override
        public synchronized Throwable getCause() {
            return cause;
        }
    }
}
