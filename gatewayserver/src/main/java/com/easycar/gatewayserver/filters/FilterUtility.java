package com.easycar.gatewayserver.filters;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class FilterUtility {

    public static final String CORRELATION_ID = "easycar-correlation-id";

    public String getCorrelationId(HttpHeaders requestHeaders) {
        if (requestHeaders.get(CORRELATION_ID) == null) {
            return null;
        }
        List<String> requestHeaderList = requestHeaders.get(CORRELATION_ID);
        if (requestHeaderList == null || requestHeaderList.stream().findFirst().isEmpty()) {
            return null;
        }
        return requestHeaderList.stream().findFirst().get();
    }

    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String name, String value) {
        return exchange.mutate()
                .request(exchange.getRequest().mutate().header(name, value).build())
                .build();
    }

    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }
}
