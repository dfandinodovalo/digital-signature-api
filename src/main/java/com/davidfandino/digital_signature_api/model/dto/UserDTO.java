package com.davidfandino.digital_signature_api.model.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public @Data class UserDTO {

    private UUID userUUID;
    private String firstName;
    private String lastName;
    private String nif;
    private LocalDateTime creationDate;
    private UserKeysDTO userKeys;

}
