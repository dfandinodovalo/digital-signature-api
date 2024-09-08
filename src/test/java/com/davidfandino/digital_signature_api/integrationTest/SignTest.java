package com.davidfandino.digital_signature_api.integrationTest;

import com.davidfandino.digital_signature_api.dto.SignDocumentDto;
import com.davidfandino.digital_signature_api.model.User;
import com.davidfandino.digital_signature_api.model.UserKeys;
import com.davidfandino.digital_signature_api.repository.UserKeysRepository;
import com.davidfandino.digital_signature_api.repository.UserRepository;
import com.davidfandino.digital_signature_api.service.SignService;
import com.davidfandino.digital_signature_api.service.UserKeysService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SignTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserKeysRepository userKeysRepository;
    @Autowired
    private UserKeysService userKeysService;

    @BeforeEach
    public void setup() {
        cleanRepositories();
    }

    @Test
    public void testSignDocumentSuccess() throws Exception {
        UserKeys userKeys = createBaseUserAndGenerateKeys();

        SignDocumentDto signDocumentDto = generateSignDocumentDto(userKeys);

        String signDocumentJson = new ObjectMapper().writeValueAsString(signDocumentDto);

        mockMvc.perform(post("/api/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signDocumentJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    public void testSignDocumentUserNotFound() throws Exception {
        SignDocumentDto signDocumentDto = generateSignDocumentDtoByNif("fakeNif");

        String signDocumentJson = new ObjectMapper().writeValueAsString(signDocumentDto);

        mockMvc.perform(post("/api/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signDocumentJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with nif "
                        + signDocumentDto.getNif() +" not found."));
    }

    @Test
    public void testSignDocumentUserKeysNotFound() throws Exception {
        User user = createBaseUserInBBDD();
        SignDocumentDto signDocumentDto = generateSignDocumentDtoByNif(user.getNif());

        String signDocumentJson = new ObjectMapper().writeValueAsString(signDocumentDto);

        mockMvc.perform(post("/api/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signDocumentJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User keys with nif "
                        + signDocumentDto.getNif() + " have not been found"));
    }

    private void cleanRepositories() {
        userKeysRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createBaseUserInBBDD() {
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

    private UserKeys createBaseUserAndGenerateKeys() throws Exception {
        User user = createBaseUserInBBDD();
        return userKeysService.generateKeys(user.getNif());
    }

    private SignDocumentDto generateSignDocumentDto(UserKeys userKeys) {
        SignDocumentDto signDocumentDto = new SignDocumentDto();
        signDocumentDto.setNif(userKeys.getUser().getNif());
        String document = "Document to sign";
        signDocumentDto.setDocumentBase64(Base64.getEncoder().encodeToString(document.getBytes()));

        return signDocumentDto;
    }

    private SignDocumentDto generateSignDocumentDtoByNif(String nif) {
        SignDocumentDto signDocumentDto = new SignDocumentDto();
        signDocumentDto.setNif(nif);
        String document = "Document to sign";
        signDocumentDto.setDocumentBase64(Base64.getEncoder().encodeToString(document.getBytes()));

        return signDocumentDto;
    }

}
