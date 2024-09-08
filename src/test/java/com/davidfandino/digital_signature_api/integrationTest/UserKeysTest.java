package com.davidfandino.digital_signature_api.integrationTest;

import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import com.davidfandino.digital_signature_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserKeysTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserKeysRepository userKeysRepository;

    @BeforeEach
    public void setup() {
        cleanRepositories();
    }

    @Test
    public void testGenerateKeysSuccess() throws Exception {
        User user = createBaseUser();
        userRepository.save(user);

        String nif = user.getNif();

        mockMvc.perform(post("/api/userkeys/generate-keys/" + nif)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Keys generated for user: " + nif));
    }

    @Test
    public void testGenerateKeysUserNotFound() throws Exception {
        String nif = "fakeNif";

        mockMvc.perform(post("/api/userkeys/generate-keys/" + nif)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGenerateKeysAlreadyGenerated() throws Exception {
        User user = createBaseUser();
        userRepository.save(user);

        String nif = user.getNif();

        mockMvc.perform(post("/api/userkeys/generate-keys/" + nif)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/userkeys/generate-keys/" + nif)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("User with NIF: " + nif + " already has keys generated.")); // Verifica el mensaje
    }

    private void cleanRepositories() {
        userKeysRepository.deleteAll();
        userRepository.deleteAll();
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
