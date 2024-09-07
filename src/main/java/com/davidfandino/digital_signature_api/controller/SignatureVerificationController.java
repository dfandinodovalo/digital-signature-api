package com.davidfandino.digital_signature_api.controller;

import com.davidfandino.digital_signature_api.dto.VerifySignatureDto;
import com.davidfandino.digital_signature_api.exception.UserKeysNotFoundException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.service.SignatureVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signature")
public class SignatureVerificationController {

    @Autowired
    private SignatureVerificationService signatureVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<String> verifySignature(@RequestBody VerifySignatureDto verifySignatureDto) {
        try {
            boolean isSignatureValid = signatureVerificationService.verifySignature(verifySignatureDto);

            if (isSignatureValid) {
                return ResponseEntity.ok("La firma es válida.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La firma no es válida.");
            }
        } catch (UserNotFoundException | UserKeysNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al verificar la firma.");
        }
    }
}