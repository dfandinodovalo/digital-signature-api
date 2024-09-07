package com.davidfandino.digital_signature_api;

import com.davidfandino.digital_signature_api.dto.UserDto;
import com.davidfandino.digital_signature_api.exception.UserAlreadyExistsException;
import com.davidfandino.digital_signature_api.exception.UserNotFoundException;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.repository.UserRepository;
import com.davidfandino.digital_signature_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setNif("12345678A");
    }

    @Test
    public void createUser_Success() throws Exception {
        when(userRepository.existsByNif(userDto.getNif()))
                .thenReturn(false);
        userService.createUser(userDto);
        verify(userRepository, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(userDto.getNif(), savedUser.getNif());
        assertEquals(userDto.getFirstName(), savedUser.getFirstName());
        assertEquals(userDto.getLastName(), savedUser.getLastName());
    }

    @Test
    public void createUser_UserAlreadyExistsException() {
        when(userRepository.existsByNif(userDto.getNif()))
                .thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userDto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void getUserByNif_Success() throws Exception {

        User user = new User();
        user.setUserUUID(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setNif("12345678A");
        user.setCreationDate(LocalDateTime.now());

        when(userRepository.getUserByNif(user.getNif()))
                .thenReturn(Optional.of(user));
        User result = userService.getUserByNif(user.getNif());

        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getNif(), result.getNif());
        assertNotNull(result.getUserUUID());

        verify(userRepository, times(1)).getUserByNif(user.getNif());
    }

    @Test
    public void getUserByNif_UserNotFoundException() {
        when(userRepository.getUserByNif(userDto.getNif()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByNif(userDto.getNif()));

        verify(userRepository, times(1)).getUserByNif(userDto.getNif());
    }

    @Test
    public void existUserWithNif_ReturnsTrue() {
        when(userRepository.existsByNif(userDto.getNif()))
                .thenReturn(true);

        Boolean exists = userService.existUserWithNif(userDto.getNif());
        assertTrue(exists);

        verify(userRepository, times(1)).existsByNif(userDto.getNif());
    }

    @Test
    public void existUserWithNif_ReturnsFalse() {
        when(userRepository.existsByNif(userDto.getNif()))
                .thenReturn(false);

        Boolean exists = userService.existUserWithNif(userDto.getNif());
        assertFalse(exists);

        verify(userRepository, times(1)).existsByNif(userDto.getNif());
    }
}
