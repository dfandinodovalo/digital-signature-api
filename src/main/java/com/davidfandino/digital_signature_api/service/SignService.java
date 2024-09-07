package com.davidfandino.digital_signature_api.service;

import com.davidfandino.digital_signature_api.dto.SignDocumentDto;
import com.davidfandino.digital_signature_api.exception.UserKeysNotFoundException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import com.davidfandino.digital_signature_api.utils.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class SignService {

    @Autowired
    private UserKeysRepository userKeysRepository;

    @Autowired
    private UserService userService;

    @Value("${app.secret-key}")
    private String secretKey;

    public String signDocument(SignDocumentDto signDocumentDto) throws Exception {
        // 1. Obtener el usuario mediante su NIF
        User user = userService.getUserByNif(signDocumentDto.getNif());
        if (user == null) {
            throw new UserNotFoundException("Usuario no encontrado con NIF: " + signDocumentDto.getNif());
        }

        // 2. Obtener las claves del usuario
        UserKeys userKeys = userKeysRepository.findByUser(user)
                .orElseThrow(() -> new UserKeysNotFoundException("No se encontraron claves para el usuario con NIF: " + signDocumentDto.getNif()));

        // 3. Desencriptar la clave privada
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), EncryptionUtil.ALGORITHM);
        String decryptedPrivateKey = EncryptionUtil.decrypt(userKeys.getPrivateKey(), secretKeySpec);
        System.out.println("\nPrivate key:" + decryptedPrivateKey);

        // 4. Convertir el documento de base64 a bytes
        byte[] documentBytes = Base64.getDecoder().decode(signDocumentDto.getDocumentBase64());

        // 5. Crear la firma del documento usando la clave privada
        PrivateKey privateKey = getPrivateKeyFromString(decryptedPrivateKey);
        byte[] signatureBytes = signData(documentBytes, privateKey);

        // 6. Retornar la firma en base64
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // Método para obtener la clave privada desde la cadena Base64
    private PrivateKey getPrivateKeyFromString(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    // Método para firmar el documento
    private byte[] signData(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }
}