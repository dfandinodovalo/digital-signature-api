package com.davidfandino.digital_signature_api.service;

import com.davidfandino.digital_signature_api.exception.UserKeysAlreadyGeneratedException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import com.davidfandino.digital_signature_api.utils.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
public class UserKeysService {

    @Autowired
    private final EncryptionUtil encryptionUtil;

    private final UserKeysRepository userKeysRepository;
    private final UserService userService;

    @Value("${app.secret-key}")
    public String secretKey;

    public UserKeysService(EncryptionUtil encryptionUtil, UserKeysRepository userKeysRepository, UserService userService) throws Exception {
        this.encryptionUtil = encryptionUtil;
        this.userKeysRepository = userKeysRepository;
        this.userService = userService;
    }

    public UserKeys generateKeys(String nif) throws Exception {
        try {
            User user = userService.getUserByNif(nif);

            if (userKeysRepository.existsByUser(user)) {
                throw new UserKeysAlreadyGeneratedException("User with NIF: " + nif + " already has keys generated.");
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), EncryptionUtil.ALGORITHM);
            String encryptedPrivateKey = encryptionUtil.encrypt(privateKey, secretKeySpec);

            UserKeys userKeys = new UserKeys(UUID.randomUUID(), publicKey, encryptedPrivateKey, user);
            return userKeysRepository.save(userKeys);

        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("User not found with NIF: " + nif);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating keys", e);
        }
    }


}
