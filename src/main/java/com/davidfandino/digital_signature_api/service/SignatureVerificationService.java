package com.davidfandino.digital_signature_api.service;

import com.davidfandino.digital_signature_api.dto.VerifySignatureDto;
import com.davidfandino.digital_signature_api.exception.UserKeysNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class SignatureVerificationService {

    @Autowired
    private UserKeysRepository userKeysRepository;

    @Autowired
    private UserService userService;

    public boolean verifySignature(VerifySignatureDto verifySignatureDto) throws Exception {
        User user = userService.getUserByNif(verifySignatureDto.getNif());
        UserKeys userKeys = userKeysRepository.findByUser(user)
                .orElseThrow(() -> new UserKeysNotFoundException("No keys were found for the user with NIF: " + verifySignatureDto.getNif()));

        String publicKeyBase64 = userKeys.getPublicKey();
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        byte[] documentBytes = Base64.getDecoder().decode(verifySignatureDto.getDocumentBase64());
        byte[] signatureBytes = Base64.getDecoder().decode(verifySignatureDto.getSignatureBase64());

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(documentBytes);

        return signature.verify(signatureBytes);
    }
}