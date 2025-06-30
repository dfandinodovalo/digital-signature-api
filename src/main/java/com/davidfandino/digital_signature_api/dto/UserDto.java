package com.davidfandino.digital_signature_api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public @Data class UserDto {

    private UUID userUUID;
    private String firstName;
    private String lastName;
    private String nif;
    private LocalDateTime creationDate;
    private UserKeysDTO userKeys;

}
