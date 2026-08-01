package com.psw.gateway.utility;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ExceptionParserUtil {

    private static final int MAX_CAUSE_DEPTH = 4;

    public Map<String, Object> exceptionParser(Exception exception) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> chain = new ArrayList<>();

        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        Throwable current = exception;
        int depth = 0;
        boolean truncated = false;
        boolean circularReference = false;

        while (current != null) {
            if (!visited.add(current)) {
                circularReference = true;
                break;
            }

            if (depth >= MAX_CAUSE_DEPTH) {
                truncated = true;
                break;
            }

            chain.add(parseThrowable(current, depth));

            current = current.getCause();
            depth++;
        }

        result.put("chain", chain);
        result.put("depth", chain.size());
        result.put("truncated", truncated);
        result.put("circularReference", circularReference);

        return result;
    }

    private Map<String, Object> parseThrowable(Throwable throwable, int depth) {
        Map<String, Object> parsed = new LinkedHashMap<>();

        parsed.put("depth", depth);
        parsed.put("type", throwable.getClass().getSimpleName());
        parsed.put("className", throwable.getClass().getName());
        parsed.put("message", throwable.getMessage());

        StackTraceElement[] stackTrace = throwable.getStackTrace();

        if (stackTrace.length > 0) {
            StackTraceElement originFrame = stackTrace[0];

            parsed.put("sourceClass", originFrame.getClassName());
            parsed.put("sourceMethod", originFrame.getMethodName());
            parsed.put("sourceFile", originFrame.getFileName());
            parsed.put("lineNumber", originFrame.getLineNumber());
            parsed.put("stackTraceAvailable", true);
        } else {
            parsed.put("stackTraceAvailable", false);
        }
        return parsed;
    }
}