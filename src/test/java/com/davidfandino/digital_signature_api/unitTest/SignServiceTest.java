package com.davidfandino.digital_signature_api.unitTest;

import com.davidfandino.digital_signature_api.dto.SignDocumentDto;
import com.davidfandino.digital_signature_api.exception.DecryptKeyErrorException;
import com.davidfandino.digital_signature_api.exception.UserKeysNotFoundException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import com.davidfandino.digital_signature_api.service.SignService;
import com.davidfandino.digital_signature_api.service.UserService;
import com.davidfandino.digital_signature_api.utils.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SignServiceTest {

    @Mock
    private UserKeysRepository userKeysRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private SignService signService;

    private User user;
    private UserKeys userKeys;
    private SignDocumentDto signDocumentDto;
    private String secretKey = "abcdefghijklmnop";
    private String privateKeyBase64;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        try {
            KeyPair keyPair = KeyGeneratorUtil.generateKeyPair();
            privateKeyBase64 = KeyGeneratorUtil.getPrivateKeyBase64(keyPair.getPrivate());

            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), EncryptionUtil.ALGORITHM);
            String encryptedPrivateKey = encryptionUtil.encrypt(privateKeyBase64, secretKeySpec);
            userKeys = new UserKeys();
            userKeys.setPrivateKey(encryptedPrivateKey);
            userKeys.setUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Init test error");
        }

        user = new User();
        user.setUserUUID(UUID.randomUUID());
        user.setNif("12345678A");
        user.setFirstName("John");

        signDocumentDto = new SignDocumentDto();
        signDocumentDto.setDocumentBase64(Base64.getEncoder().encodeToString("Test document".getBytes()));
        signDocumentDto.setNif(user.getNif());

        signService.getClass().getDeclaredField("secretKey").set(signService, secretKey);
    }


    @Test
    public void signDocument_Success() throws Exception {
        when(userService.getUserByNif(user.getNif())).thenReturn(user);
        when(userKeysRepository.findByUser(user)).thenReturn(Optional.of(userKeys));

        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), encryptionUtil.ALGORITHM);
        when(encryptionUtil.decrypt(userKeys.getPrivateKey(), secretKeySpec)).thenReturn(privateKeyBase64);

        String signature = signService.signDocument(signDocumentDto);

        assertNotNull(signature);
        verify(userService, times(1)).getUserByNif(user.getNif());
        verify(userKeysRepository, times(1)).findByUser(user);
    }

    @Test
    public void signDocument_UserNotFoundException() {
        when(userService.getUserByNif(user.getNif())).thenThrow(new UserNotFoundException("User not found"));
        assertThrows(UserNotFoundException.class, () -> signService.signDocument(signDocumentDto));

        verify(userService, times(1)).getUserByNif(user.getNif());
        verify(userKeysRepository, never()).findByUser(any(User.class));
    }

    @Test
    public void signDocument_UserKeysNotFoundException() {
        when(userService.getUserByNif(user.getNif())).thenReturn(user);
        when(userKeysRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(UserKeysNotFoundException.class, () -> signService.signDocument(signDocumentDto));

        verify(userService, times(1)).getUserByNif(user.getNif());
        verify(userKeysRepository, times(1)).findByUser(user);
    }

    @Test
    public void signDocument_FailureToDecryptPrivateKey() throws Exception {
        when(userService.getUserByNif(user.getNif())).thenReturn(user);
        when(userKeysRepository.findByUser(user)).thenReturn(Optional.of(userKeys));

        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), EncryptionUtil.ALGORITHM);
        when(encryptionUtil.decrypt(userKeys.getPrivateKey(), secretKeySpec)).thenThrow(new DecryptKeyErrorException("Error decrypting private key"));

        assertThrows(DecryptKeyErrorException.class, () -> signService.signDocument(signDocumentDto));

        verify(userService, times(1)).getUserByNif(user.getNif());
        verify(userKeysRepository, times(1)).findByUser(user);
    }


    private static class KeyGeneratorUtil {

        public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            return keyPairGen.generateKeyPair();
        }

        public static String getPrivateKeyBase64(PrivateKey privateKey) {
            return Base64.getEncoder().encodeToString(privateKey.getEncoded());
        }

        public static String getPublicKeyBase64(PublicKey publicKey) {
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }

    }

}
