package com.davidfandino.digital_signature_api.exception;

public class UserKeysAlreadyGeneratedException extends RuntimeException {

    public UserKeysAlreadyGeneratedException(String message) {
        super(message);
    }
}
