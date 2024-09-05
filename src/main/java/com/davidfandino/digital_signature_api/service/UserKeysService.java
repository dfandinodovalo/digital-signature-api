package com.davidfandino.digital_signature_api.service;

import com.davidfandino.digital_signature_api.exception.UserKeysAlreadyGeneratedException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import com.davidfandino.digital_signature_api.utils.EncryptionUtil;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
public class UserKeysService {

    private final UserKeysRepository userKeysRepository;
    private final UserService userService;
    private final SecretKey secretKey;

    public UserKeysService(UserKeysRepository userKeysRepository, UserService userService) throws Exception {
        this.userKeysRepository = userKeysRepository;
        this.userService = userService;
        this.secretKey = EncryptionUtil.generateAESKey();
    }

    public void generateKeys(String nif) throws Exception {
        try {
            User user = userService.getUserByNif(nif);

            if (userKeysRepository.existsByUserUUID(user.getUserUUID())) {
                throw new UserKeysAlreadyGeneratedException("User with NIF: " + nif + " already has keys generated.");
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            String encryptedPrivateKey = EncryptionUtil.encrypt(privateKey, secretKey);

            UserKeys userKeys = new UserKeys(UUID.randomUUID(), user.getUserUUID(), publicKey, encryptedPrivateKey, user);
            userKeysRepository.save(userKeys);

        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("User not found with NIF: " + nif);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating keys", e);
        }
    }


}
