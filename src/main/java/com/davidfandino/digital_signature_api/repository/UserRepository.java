package com.davidfandino.digital_signature_api.repository;

import com.davidfandino.digital_signature_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Boolean existsByNif(String nif);

    Optional<User> getUserByNif(String nif);

}

