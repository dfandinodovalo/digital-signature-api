package com.davidfandino.digital_signature_api;

import com.davidfandino.digital_signature_api.exception.UserKeysAlreadyGeneratedException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import com.davidfandino.digital_signature_api.service.UserKeysService;
import com.davidfandino.digital_signature_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserKeysServiceTest {

    @Mock
    private UserKeysRepository userKeysRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserKeysService userKeysService;

    private User user;
    private String secretKey = "mySecretKey12345";

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        user = new User();
        user.setUserUUID(UUID.randomUUID());
        user.setNif("12345678A");
        user.setFirstName("John Doe");

        userKeysService.getClass().getDeclaredField("secretKey").set(userKeysService, secretKey);
    }

    @Test
    public void generateKeys_Success() throws Exception {
        when(userService.getUserByNif(user.getNif())).thenReturn(user);
        when(userKeysRepository.existsByUser(user)).thenReturn(false);
        when(userKeysRepository.save(any(UserKeys.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserKeys generatedUserKeys = userKeysService.generateKeys(user.getNif());

        assertNotNull(generatedUserKeys);
        assertNotNull(generatedUserKeys.getPublicKey());
        assertNotNull(generatedUserKeys.getPrivateKey());

        assertNotEquals(generatedUserKeys.getPublicKey(), generatedUserKeys.getPrivateKey());

        verify(userKeysRepository, times(1)).save(any(UserKeys.class));
    }

    @Test
    public void generateKeys_UserNotFoundException() {
        when(userService.getUserByNif(user.getNif())).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> userKeysService.generateKeys(user.getNif()));

        verify(userKeysRepository, never()).save(any(UserKeys.class));
        verify(userKeysRepository, never()).existsByUser(any(User.class));
    }


    @Test
    public void generateKeys_UserKeysAlreadyGeneratedException() {
        User userWithGeneratedKeys = cloneUser(user);
        when(userService.getUserByNif(userWithGeneratedKeys.getNif())).thenReturn(userWithGeneratedKeys);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userKeysRepository.existsByUser(userCaptor.capture())).thenReturn(true);

        assertThrows(UserKeysAlreadyGeneratedException.class, () -> userKeysService.generateKeys(userWithGeneratedKeys.getNif()));
        verify(userKeysRepository, never()).save(any(UserKeys.class));
        assertEquals(userWithGeneratedKeys.getNif(), userCaptor.getValue().getNif());
    }


    private User cloneUser(User originalUser) {
        UserKeys clonedUserKeys = null;
        if (originalUser.getUserKeys() != null) {
            clonedUserKeys = new UserKeys();
            clonedUserKeys.setUserKeyUUID(originalUser.getUserKeys().getUserKeyUUID());
            clonedUserKeys.setPublicKey(originalUser.getUserKeys().getPublicKey());
            clonedUserKeys.setPrivateKey(originalUser.getUserKeys().getPrivateKey());
            clonedUserKeys.setUser(null);
        }

        User clonedUser = new User();
        clonedUser.setUserUUID(originalUser.getUserUUID());
        clonedUser.setFirstName(originalUser.getFirstName());
        clonedUser.setLastName(originalUser.getLastName());
        clonedUser.setNif(originalUser.getNif());
        clonedUser.setCreationDate(originalUser.getCreationDate());

        if (clonedUserKeys != null) {
            clonedUserKeys.setUser(clonedUser);
            clonedUser.setUserKeys(clonedUserKeys);
        }

        return clonedUser;
    }


}
