package com.davidfandino.digital_signature_api.integrationTest;

import com.davidfandino.digital_signature_api.dto.UserDto;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        cleanRepositories();
    }

    @Test
    public void testCreateUserSuccess() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setNif("12345678A");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");

        String userJson = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().json(userJson));
    }

    @Test
    public void testCreateUserAlreadyExists() throws Exception {
        saveBaseUserInBBDD();

        UserDto userDto = new UserDto();
        userDto.setNif("12345678A");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");

        String userJson = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("User with NIF " + userDto.getNif() + " already exists")); // Verifica el mensaje de error
    }


    private void cleanRepositories() {
        userRepository.deleteAll();
    }

    private User saveBaseUserInBBDD() {
        return userRepository.save(createBaseUser());
    }


    private User createBaseUser() {
        User user = new User();
        user.setNif("12345678A");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCreationDate(LocalDateTime.now());

        return user;
    }
}
