package com.davidfandino.digital_signature_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public @Data class VerifySignatureDto {
    private String documentBase64;
    private String signatureBase64;
    private String nif;
}
