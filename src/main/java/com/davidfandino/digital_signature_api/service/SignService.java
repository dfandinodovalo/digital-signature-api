package com.davidfandino.digital_signature_api.service;

import com.davidfandino.digital_signature_api.dto.SignDocumentDto;
import com.davidfandino.digital_signature_api.exception.*;
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
    private EncryptionUtil encryptionUtil;

    @Autowired
    private UserService userService;

    @Value("${app.secret-key}")
    public String secretKey;

    public String signDocument(SignDocumentDto signDocumentDto) throws Exception {
        try {
            User user = userService.getUserByNif(signDocumentDto.getNif());
            if (user == null) {
                throw new UserNotFoundException("User not found with NIF: " + signDocumentDto.getNif());
            }

            UserKeys userKeys = userKeysRepository.findByUser(user)
                    .orElseThrow(() -> new UserKeysNotFoundException("The keys for the user with NIF " + signDocumentDto.getNif() +" were not found."));

            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), encryptionUtil.ALGORITHM);
            String decryptedPrivateKey = encryptionUtil.decrypt(userKeys.getPrivateKey(), secretKeySpec);

            byte[] documentBytes = Base64.getDecoder().decode(signDocumentDto.getDocumentBase64());

            PrivateKey privateKey = getPrivateKeyFromString(decryptedPrivateKey);
            byte[] signatureBytes = signData(documentBytes, privateKey);

            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        } catch (UserKeysNotFoundException e) {
            throw new UserKeysNotFoundException(e.getMessage());
        } catch (DecryptKeyErrorException e){
            throw new DecryptKeyErrorException(e.getMessage());
        } catch (Exception e) {
            throw new SignDocumentErrorException(e.getMessage());
        }
    }

    private PrivateKey getPrivateKeyFromString(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private byte[] signData(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }
}