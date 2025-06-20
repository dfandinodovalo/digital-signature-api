package com.davidfandino.digital_signature_api.model.dto;
import com.davidfandino.digital_signature_api.model.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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