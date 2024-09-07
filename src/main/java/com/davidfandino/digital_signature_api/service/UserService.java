package com.davidfandino.digital_signature_api.service;

import com.davidfandino.digital_signature_api.dto.UserDto;
import com.davidfandino.digital_signature_api.exception.UserAlreadyExistsException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(UserDto userDto) throws UserAlreadyExistsException {

        if (existUserWithNif(userDto.getNif())) {
            throw new UserAlreadyExistsException("User with NIF " + userDto.getNif() + " already exists");
        }

        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setNif(userDto.getNif());

        user.setUserUUID(UUID.randomUUID());
        user.setCreationDate(LocalDateTime.now());

        userRepository.save(user);
    }

    public User getUserByNif(String nif) throws UserNotFoundException {
        return userRepository.getUserByNif(nif).orElseThrow(() -> new UserNotFoundException("User with NIF " + nif + " not found."));
    }

    public Boolean existUserWithNif(String nif) {
        return userRepository.existsByNif(nif);
    }

}
