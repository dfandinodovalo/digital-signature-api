package com.davidfandino.digital_signature_api.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public @Data class UserKeysDTO {

    private UUID userKeyUUID;
    private String publicKey;
}