package com.davidfandino.digital_signature_api.exception;

public class UserKeysNotFoundException extends RuntimeException {

    public UserKeysNotFoundException(String message) {
        super(message);
    }
}
