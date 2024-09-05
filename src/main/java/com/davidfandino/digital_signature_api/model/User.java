package com.davidfandino.digital_signature_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "signature_user")
public @Data class User {

    @Id
    private UUID userUUID;
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String nif;
    private LocalDateTime creationDate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserKeys userKeys;

}
