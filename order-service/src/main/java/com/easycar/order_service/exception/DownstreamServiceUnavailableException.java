package com.easycar.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class DownstreamServiceUnavailableException extends RuntimeException {
    public DownstreamServiceUnavailableException(String serviceName) {
        super(serviceName + " is currently unavailable");
    }
}
