package org.meigo.tulpar.server.utils;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryRequestLog implements RequestLog {
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void log(Request request, Response response) {
        // Формируем запись лога
        String logEntry = String.format("%s - %s %s %d",
                request.getRemoteAddr(),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus());
        logs.add(logEntry);
    }

    public List<String> getLogs() {
        return logs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        synchronized (logs) {
            for (String log : logs) {
                sb.append(log).append("\n");
            }
        }
        return sb.toString();
    }
}
