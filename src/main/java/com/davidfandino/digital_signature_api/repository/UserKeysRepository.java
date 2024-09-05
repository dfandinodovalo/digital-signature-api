package com.davidfandino.digital_signature_api.repository;

import com.davidfandino.digital_signature_api.model.UserKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserKeysRepository extends JpaRepository<UserKeys, String> {

    Boolean existsByUserUUID(UUID userUUID);

}