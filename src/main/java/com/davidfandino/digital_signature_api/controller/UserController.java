package com.davidfandino.digital_signature_api.controller;

import com.davidfandino.digital_signature_api.dto.UserDto;
import com.davidfandino.digital_signature_api.exception.UserAlreadyExistsException;
import com.davidfandino.digital_signature_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody UserDto userDto) {
        try {
            userService.createUser(userDto);
            return ResponseEntity.ok(userDto);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}
