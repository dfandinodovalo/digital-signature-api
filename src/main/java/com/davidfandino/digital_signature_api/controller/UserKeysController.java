package com.davidfandino.digital_signature_api.controller;

import com.davidfandino.digital_signature_api.exception.UserKeysAlreadyGeneratedException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.service.UserKeysService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/userkeys")
public class UserKeysController {

    private final UserKeysService userKeysService;

    public UserKeysController(UserKeysService userKeysService) {
        this.userKeysService = userKeysService;
    }

    @PostMapping("/generate-keys/{nif}")
    public ResponseEntity<String> generateKeys(@PathVariable String nif) {
        try {
            userKeysService.generateKeys(nif);
            return ResponseEntity.ok("Keys generated for user: " + nif);
        }   catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserKeysAlreadyGeneratedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
