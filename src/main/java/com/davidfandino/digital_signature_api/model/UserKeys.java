package com.davidfandino.digital_signature_api.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public @Data class UserKeys {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userKeyUUID;

    @Lob
    private String publicKey;

    @Lob
    private String privateKey;

    @OneToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;
}