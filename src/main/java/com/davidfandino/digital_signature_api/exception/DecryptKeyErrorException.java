package com.davidfandino.digital_signature_api.exception;

public class DecryptKeyErrorException extends RuntimeException {

    public DecryptKeyErrorException(String message) {
        super(message);
    }
}
