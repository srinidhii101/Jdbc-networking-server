package com.dal.models;

import java.util.HashMap;
import java.util.Map;

public class OutputFrame {
    String protocol;
    String statusCode;
    String status;
    Map<String, String> headerValues = new HashMap<>();
    String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getHeaderValues() {
        return headerValues;
    }

    public void setHeaderValues(Map<String, String> headerValues) {
        this.headerValues = headerValues;
    }

    public static String prepareOutputFrame(OutputFrame outputFrame) {
        StringBuilder output = new StringBuilder();
        output.append(outputFrame.getProtocol()).append(" ").append(outputFrame.getStatusCode()).append(" ").append(outputFrame.getStatus()).append("\n");
        for (Map.Entry<String, String> entry : outputFrame.getHeaderValues().entrySet()) {
            output.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        if (outputFrame.getBody() != null) output.append(outputFrame.getBody());
        return output.toString();
    }
}
