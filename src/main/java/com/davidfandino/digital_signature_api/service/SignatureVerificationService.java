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
        // 1. Obtener el usuario a través del NIF
        User user = userService.getUserByNif(verifySignatureDto.getNif());

        // 2. Obtener las claves del usuario
        UserKeys userKeys = userKeysRepository.findByUserUUID(user.getUserUUID())
                .orElseThrow(() -> new UserKeysNotFoundException("No se encontraron claves para el usuario con NIF: " + verifySignatureDto.getNif()));

        // 3. Obtener la clave pública del usuario (en formato base64)
        String publicKeyBase64 = userKeys.getPublicKey();

        // 4. Decodificar la clave pública de base64 a bytes
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

        // 5. Convertir los bytes a un objeto PublicKey
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // 6. Decodificar el documento y la firma de base64
        byte[] documentBytes = Base64.getDecoder().decode(verifySignatureDto.getDocumentBase64());
        byte[] signatureBytes = Base64.getDecoder().decode(verifySignatureDto.getSignatureBase64());

        // 7. Verificar la firma
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(documentBytes);

        // 8. Retornar si la firma es válida o no
        return signature.verify(signatureBytes);
    }
}