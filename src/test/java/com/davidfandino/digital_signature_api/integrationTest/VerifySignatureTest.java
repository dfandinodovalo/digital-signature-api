package com.davidfandino.digital_signature_api.integrationTest;

import com.davidfandino.digital_signature_api.dto.SignDocumentDto;
import com.davidfandino.digital_signature_api.dto.VerifySignatureDto;
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

import java.time.LocalDateTime;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class VerifySignatureTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserKeysRepository userKeysRepository;
    @Autowired
    private UserKeysService userKeysService;
    @Autowired
    private SignService signService;


    @BeforeEach
    public void setup() {
        cleanRepositories();
    }

    @Test
    public void testVerifySignatureSuccess() throws Exception {

        VerifySignatureDto verifySignatureDto = signValidDocument();
        String verifySignatureJson = new ObjectMapper().writeValueAsString(verifySignatureDto);

        mockMvc.perform(post("/api/signature/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifySignatureJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    public void testVerifySignatureUserNotFound() throws Exception {
        VerifySignatureDto verifySignatureDto = signValidDocument();
        verifySignatureDto.setNif("fakeNif");
        String verifySignatureJson = new ObjectMapper().writeValueAsString(verifySignatureDto);

        mockMvc.perform(post("/api/signature/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifySignatureJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with nif "
                        + verifySignatureDto.getNif() +" not found."));
    }
    @Test
    public void testVerifySignatureUserKeysNotFound() throws Exception {

        VerifySignatureDto signDocumentWithoutUserkeys = signDocumentWithoutUserkeys();
        String verifySignatureJson = new ObjectMapper().writeValueAsString(signDocumentWithoutUserkeys);

        mockMvc.perform(post("/api/signature/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifySignatureJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User keys with nif " +
                        signDocumentWithoutUserkeys.getNif() + " have not been found"));
    }

    @Test
    public void testVerifySignatureInvalidSignature() throws Exception {
        UserKeys otherUserKeys = createBaseUserAndGenerateKeys("otherUserNif");
        VerifySignatureDto verifySignatureDto = signValidDocument();
        verifySignatureDto.setNif(otherUserKeys.getUser().getNif());

        String verifySignatureJson = new ObjectMapper().writeValueAsString(verifySignatureDto);

        mockMvc.perform(post("/api/signature/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifySignatureJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The signature is not valid.")); // Verifica el mensaje de error
    }

    @Test
    public void testVerifySignatureInternalServerError() throws Exception {
        VerifySignatureDto verifySignatureDto = signValidDocument();
        verifySignatureDto.setSignatureBase64("invalidSignatureBase64");
        String verifySignatureJson = new ObjectMapper().writeValueAsString(verifySignatureDto);

        mockMvc.perform(post("/api/signature/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifySignatureJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }


    private void cleanRepositories() {
        userKeysRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createBaseUserInBBDD() {
        return createBaseUserInBBDD("12345678A");
    }

    private User createBaseUserInBBDD(String nif) {
        return userRepository.save(createBaseUser(nif));
    }

    private User createBaseUser(String nif) {
        User user = new User();
        user.setNif(nif);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCreationDate(LocalDateTime.now());

        return user;
    }

    private UserKeys createBaseUserAndGenerateKeys() throws Exception {
        return createBaseUserAndGenerateKeys("12345678A");
    }

    private UserKeys createBaseUserAndGenerateKeys(String nif) throws Exception {
        User user = createBaseUserInBBDD(nif);
        return userKeysService.generateKeys(user.getNif());
    }

    private SignDocumentDto generateSignDocumentDto(String nif) {
        SignDocumentDto signDocumentDto = new SignDocumentDto();
        signDocumentDto.setNif(nif);
        String document = "Document to sign";
        signDocumentDto.setDocumentBase64(Base64.getEncoder().encodeToString(document.getBytes()));

        return signDocumentDto;
    }

    private String signDocument(SignDocumentDto signDocumentDto) throws Exception {
        return signService.signDocument(signDocumentDto);
    }

    private VerifySignatureDto signValidDocument() throws Exception {
        UserKeys userKeys = createBaseUserAndGenerateKeys();
        SignDocumentDto signDocumentDto = generateSignDocumentDto(userKeys.getUser().getNif());

        VerifySignatureDto verifySignatureDto = new VerifySignatureDto();
        verifySignatureDto.setSignatureBase64(signDocument(signDocumentDto));
        verifySignatureDto.setDocumentBase64(signDocumentDto.getDocumentBase64());
        verifySignatureDto.setNif(userKeys.getUser().getNif());

        return verifySignatureDto;
    }

    private VerifySignatureDto signDocumentWithoutUserkeys() throws Exception {
        User user = createBaseUserInBBDD();

        VerifySignatureDto verifySignatureDto = new VerifySignatureDto();
        verifySignatureDto.setNif(user.getNif());
        String document = "Document to verify";
        verifySignatureDto.setDocumentBase64(Base64.getEncoder().encodeToString(document.getBytes()));
        verifySignatureDto.setSignatureBase64("invalidSignature");

        return verifySignatureDto;
    }



}
