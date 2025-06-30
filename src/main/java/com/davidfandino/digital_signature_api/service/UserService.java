package com.davidfandino.digital_signature_api.service;

import com.davidfandino.digital_signature_api.dto.UserDto;
import com.davidfandino.digital_signature_api.exception.UserAlreadyExistsException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.dto.UserKeysDTO;
import com.davidfandino.digital_signature_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserDto UserDto) throws UserAlreadyExistsException {

        if (existUserWithNif(UserDto.getNif())) {
            throw new UserAlreadyExistsException("User with NIF " + UserDto.getNif() + " already exists");
        }

        User user = new User();
        user.setFirstName(UserDto.getFirstName());
        user.setLastName(UserDto.getLastName());
        user.setNif(UserDto.getNif().toUpperCase());

        user.setUserUUID(UUID.randomUUID());
        user.setCreationDate(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getUserByNif(String nif) throws UserNotFoundException {
        return userRepository.getUserByNif(nif).orElseThrow(() -> new UserNotFoundException("User with NIF " + nif + " not found."));
    }


    public List<UserDto> getUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserDto UserDto = new UserDto();
            UserDto.setFirstName(user.getFirstName());
            UserDto.setLastName(user.getLastName());
            UserDto.setNif(user.getNif());
            UserDto.setUserUUID(user.getUserUUID());
            UserDto.setCreationDate(user.getCreationDate());

            UserKeys userKeys = user.getUserKeys();
            if (userKeys != null) {
                UserKeysDTO dto = new UserKeysDTO();
                dto.setPublicKey(userKeys.getPublicKey());
                dto.setUserKeyUUID(userKeys.getUserKeyUUID());
                UserDto.setUserKeys(dto);
            }
            return UserDto;
        }).collect(Collectors.toList());

    }

    public Boolean existUserWithNif(String nif) {
        return userRepository.existsByNif(nif);
    }

}
