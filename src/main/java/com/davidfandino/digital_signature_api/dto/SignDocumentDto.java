package com.davidfandino.digital_signature_api.dto;

import lombok.Data;

public @Data class SignDocumentDto {
    private String documentBase64;
    private String nif;
}
