package com.davidfandino.digital_signature_api.controller;

import com.davidfandino.digital_signature_api.dto.SignDocumentDto;
import com.davidfandino.digital_signature_api.exception.UserKeysNotFoundException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.service.SignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sign")
public class SignController {

    @Autowired
    private SignService signService;

    @PostMapping
    public ResponseEntity<String> signDocument(@RequestBody SignDocumentDto signDocumentDto) {
        try {
            String signatureBase64 = signService.signDocument(signDocumentDto);
            return ResponseEntity.ok(signatureBase64);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with nif "
                    + signDocumentDto.getNif() +" not found.");
        } catch (UserKeysNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User keys with nif "
                    + signDocumentDto.getNif() + " have not been found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when signing the document.");
        }
    }

}
