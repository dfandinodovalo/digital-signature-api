package com.davidfandino.digital_signature_api.unitTest;

import com.davidfandino.digital_signature_api.dto.VerifySignatureDto;
import com.davidfandino.digital_signature_api.exception.UserKeysNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import com.davidfandino.digital_signature_api.service.SignatureVerificationService;
import com.davidfandino.digital_signature_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.*;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SignatureVerificationServiceTest {

    @Mock
    private UserKeysRepository userKeysRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SignatureVerificationService signatureVerificationService;

    private User user;
    private UserKeys userKeys;
    private VerifySignatureDto verifySignatureDto;

    @BeforeEach
    public void setUp() throws Exception {
        user = new User();
        user.setUserUUID(UUID.randomUUID());
        user.setNif("12345678A");

        KeyPair keyPair = KeyGeneratorUtil.generateKeyPair();
        String publicKeyBase64 = KeyGeneratorUtil.getPublicKeyBase64(keyPair.getPublic());
        String privateKeyBase64 = KeyGeneratorUtil.getPrivateKeyBase64(keyPair.getPrivate());

        userKeys = new UserKeys();
        userKeys.setPublicKey(publicKeyBase64);
        userKeys.setUser(user);

        String document = "Test document";
        byte[] documentBytes = document.getBytes();

        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initSign(keyPair.getPrivate());
        signatureInstance.update(documentBytes);
        byte[] signatureBytes = signatureInstance.sign();

        String documentBase64 = Base64.getEncoder().encodeToString(documentBytes);
        String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

        verifySignatureDto = new VerifySignatureDto();
        verifySignatureDto.setNif(user.getNif());
        verifySignatureDto.setDocumentBase64(documentBase64);
        verifySignatureDto.setSignatureBase64(signatureBase64);
    }


    @Test
    public void verifySignature_Success() throws Exception {
        when(userService.getUserByNif(user.getNif())).thenReturn(user);
        when(userKeysRepository.findByUser(user)).thenReturn(Optional.of(userKeys));

        KeyPair keyPair = KeyGeneratorUtil.generateKeyPair();
        String publicKeyBase64 = KeyGeneratorUtil.getPublicKeyBase64(keyPair.getPublic());

        userKeys.setPublicKey(publicKeyBase64);

        String document = "Test document";
        byte[] documentBytes = document.getBytes();

        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initSign(keyPair.getPrivate());
        signatureInstance.update(documentBytes);
        byte[] signatureBytes = signatureInstance.sign();

        String documentBase64 = Base64.getEncoder().encodeToString(documentBytes);
        String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

        verifySignatureDto.setDocumentBase64(documentBase64);
        verifySignatureDto.setSignatureBase64(signatureBase64);

        boolean isValid = signatureVerificationService.verifySignature(verifySignatureDto);

        assertTrue(isValid);
        verify(userService, times(1)).getUserByNif(user.getNif());
        verify(userKeysRepository, times(1)).findByUser(user);
    }

    @Test
    public void verifySignature_UserKeysNotFoundException() {
        when(userService.getUserByNif(user.getNif())).thenReturn(user);
        when(userKeysRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(UserKeysNotFoundException.class, () -> signatureVerificationService.verifySignature(verifySignatureDto));

        verify(userService, times(1)).getUserByNif(user.getNif());
        verify(userKeysRepository, times(1)).findByUser(user);
    }

    @Test
    public void verifySignature_InvalidSignature() throws Exception {
        when(userService.getUserByNif(user.getNif())).thenReturn(user);
        when(userKeysRepository.findByUser(user)).thenReturn(Optional.of(userKeys));

        KeyPair keyPair = KeyGeneratorUtil.generateKeyPair();
        String publicKeyBase64 = KeyGeneratorUtil.getPublicKeyBase64(keyPair.getPublic());

        userKeys.setPublicKey(publicKeyBase64);

        String document = "Test document";
        byte[] documentBytes = document.getBytes();

        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initSign(keyPair.getPrivate());
        signatureInstance.update(documentBytes);
        byte[] signatureBytes = signatureInstance.sign();

        signatureBytes[0] ^= 0xFF;

        String documentBase64 = Base64.getEncoder().encodeToString(documentBytes);
        String invalidSignatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

        verifySignatureDto.setDocumentBase64(documentBase64);
        verifySignatureDto.setSignatureBase64(invalidSignatureBase64);

        // Ejecutar el servicio de verificación de firma
        boolean isValid = signatureVerificationService.verifySignature(verifySignatureDto);

        // Verificar que la firma es inválida
        assertFalse(isValid);
        verify(userService, times(1)).getUserByNif(user.getNif());
        verify(userKeysRepository, times(1)).findByUser(user);
    }

}