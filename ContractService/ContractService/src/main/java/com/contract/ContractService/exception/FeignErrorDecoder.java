package com.contract.ContractService.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        return switch (response.status()) {

            case 404 -> new ResourceNotFoundException("Property not found");

            case 409 -> new PropertyNotAvailableException("Property unavailable");

            default -> new RuntimeException("External service error");
        };
    }
}