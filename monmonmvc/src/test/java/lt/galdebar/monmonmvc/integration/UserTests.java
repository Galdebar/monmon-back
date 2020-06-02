package lt.galdebar.monmonmvc.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.InternetProtocol;
import com.github.dockerjava.api.model.Ports;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.LinkUsersTokenEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.UserEmailChangeTokenEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.UserRegistrationTokenEntity;
import lt.galdebar.monmonmvc.persistence.domain.dto.LoginAttemptDTO;
import lt.galdebar.monmonmvc.persistence.repositories.LinkUsersTokenRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserEmailChangeTokenRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserRegistrationTokenRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.EmailSenderService;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(initializers = {TestContainersConfig.Initializer.class})
@SpringBootTest
public class UserTests {

    @Autowired
    private EmailSenderService emailSenderService;

    private GreenMail greenMail;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserRegistrationTokenRepo userRegistrationTokenRepo;

    @Autowired
    private UserEmailChangeTokenRepo userEmailChangeTokenRepo;

    @Autowired
    private LinkUsersTokenRepo linkUsersTokenRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext wac;


    private MockMvc mvc;
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.mvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();
        this.objectMapper = new ObjectMapper();
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.setUser("username", "secret");
        greenMail.start();
        userRepo.deleteAll();
        userRegistrationTokenRepo.deleteAll();
        userEmailChangeTokenRepo.deleteAll();
        linkUsersTokenRepo.deleteAll();
    }


    @After
    public void afterEach() {
        userRepo.deleteAll();
        userRegistrationTokenRepo.deleteAll();
        userEmailChangeTokenRepo.deleteAll();
        linkUsersTokenRepo.deleteAll();
        greenMail.stop();
    }

    @Test
    public void givenContext_whenSendEmail_thenReceiveEmail() throws MessagingException, IOException {
        String testEmail = "test@email.com";
        String testToken = "iauwhdiuhawd";

        emailSenderService.sendLinkUsersConfirmationEmail(testEmail, testToken);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        assertEquals(1, receivedMessages.length);

        MimeMessage currentMEssage = receivedMessages[0];

        assertEquals(testEmail, currentMEssage.getAllRecipients()[0].toString());
        assertTrue(currentMEssage.getContent().toString().contains(testToken));
    }

    @Test
    public void givenStandardUser_whenRegister_thenAddUserAndTokenAndSendEmail() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String registerResponse;
        String registrationTokenIDFromEmail;
        UserRegistrationTokenEntity registrationToken;
        UserEntity registeredUser;
        MimeMessage[] receivedMessages;


        registerResponse = registerUser(testEmail, testPassword);
        assertNotNull(registerResponse);
        assertFalse(registerResponse.trim().isEmpty());
        assertTrue(registerResponse.trim().toLowerCase().contains("success"));


        receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
        assertTrue(receivedMessages[0].getContent().toString().contains("register"));

        registrationTokenIDFromEmail = getTokenFromString(receivedMessages[0].getContent().toString());
        assertNotNull(registrationTokenIDFromEmail);
        assertFalse(registrationTokenIDFromEmail.trim().isEmpty());

        registrationToken = userRegistrationTokenRepo.findByToken(registrationTokenIDFromEmail);
        assertNotNull(registrationToken);

        registeredUser = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(registeredUser);
        assertFalse(registeredUser.isValidated());

        assertEquals(registrationToken.getUser().getId(), registeredUser.getId());
    }

    @Test
    public void givenExistingEmail_whenRegister_thenReturnBadRequest() throws Exception {
        String testEmail = "user1@email.com";
        String testPassword = "password";

        registerUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", testPassword);

        String secondResponse = mvc.perform(post("/user/register")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(secondResponse);
        assertFalse(secondResponse.trim().isEmpty());
        assertTrue(secondResponse.toLowerCase().contains("exists"));
    }

    @Test
    public void givenIncorrectEmail_whenRegister_thenReturnBadRequest() throws Exception {
        String testEmail = "iuwaqd";
        String testPassword = "password";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", testPassword);

        String secondResponse = mvc.perform(post("/user/register")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(secondResponse);
        assertFalse(secondResponse.trim().isEmpty());
        assertTrue(secondResponse.toLowerCase().contains("invalid"));
    }

    @Test
    public void givenEmptyEmail_whenRegister_thenReturnBadRequest() throws Exception {
        String testEmail = "";
        String testPassword = "password";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", testPassword);

        String secondResponse = mvc.perform(post("/user/register")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(secondResponse);
        assertFalse(secondResponse.trim().isEmpty());
        assertTrue(secondResponse.toLowerCase().contains("invalid"));
    }

    @Test
    public void givenBlankEmail_whenRegister_thenReturnBadRequest() throws Exception {
        String testEmail = "       ";
        String testPassword = "password";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", testPassword);

        String secondResponse = mvc.perform(post("/user/register")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(secondResponse);
        assertFalse(secondResponse.trim().isEmpty());
        assertTrue(secondResponse.toLowerCase().contains("invalid"));
    }

    @Test
    public void givenBlankPassword_whenRegister_thenReturnBadRequest() throws Exception {
        String testEmail = "user1@email.com";
        String testPassword = "";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", testPassword);

        String secondResponse = mvc.perform(post("/user/register")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(secondResponse);
        assertFalse(secondResponse.trim().isEmpty());
        assertTrue(secondResponse.toLowerCase().contains("password"));
    }

    @Test
    public void givenEmptyPassword_whenRegister_thenReturnBadRequest() throws Exception {
        String testEmail = "user1@email.com";
        String testPassword = "     ";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", testPassword);

        String secondResponse = mvc.perform(post("/user/register")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(secondResponse);
        assertFalse(secondResponse.trim().isEmpty());
        assertTrue(secondResponse.toLowerCase().contains("password"));
    }

    @Test
    public void givenToken_whenConfirmRegistration_thenOkResponseAndValidateUser() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";


        String registerResponse = registerUser(testEmail, testPassword);
        assertNotNull(registerResponse);
        assertFalse(registerResponse.trim().isEmpty());
        assertTrue(registerResponse.trim().toLowerCase().contains("success"));


        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
        assertTrue(receivedMessages[0].getContent().toString().contains("register"));
        String registrationLink = getRelativeLink(receivedMessages[0].getContent().toString());

        String confirmResponse = mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserEntity registeredUser = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(registeredUser);
        assertTrue(registeredUser.isValidated());
    }

    @Test
    public void givenBadToken_whenConfirmRegistration_thenReturnBadResponse() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";


        String registerResponse = registerUser(testEmail, testPassword);
        assertNotNull(registerResponse);
        assertFalse(registerResponse.trim().isEmpty());
        assertTrue(registerResponse.trim().toLowerCase().contains("success"));


        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
        assertTrue(receivedMessages[0].getContent().toString().contains("register"));
        String registrationLink = getRelativeLink(receivedMessages[0].getContent().toString());
        registrationLink += "iahwikdhaw";

        String confirmResponse = mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        UserEntity registeredUser = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(registeredUser);
        assertFalse(registeredUser.isValidated());
    }

    @Test
    public void givenEmptyToken_whenConfirmRegistration_thenReturnNotFound() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String testLink = "/user/register/confirm/";


        String registerResponse = registerUser(testEmail, testPassword);
        assertNotNull(registerResponse);
        assertFalse(registerResponse.trim().isEmpty());
        assertTrue(registerResponse.trim().toLowerCase().contains("success"));


        String confirmResponse = mvc.perform(get(testLink))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        UserEntity registeredUser = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(registeredUser);
        assertFalse(registeredUser.isValidated());
    }

    @Test
    public void givenBlankToken_whenConfirmRegistration_thenReturnBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String testLink = "/user/register/confirm/          ";


        String registerResponse = registerUser(testEmail, testPassword);
        assertNotNull(registerResponse);
        assertFalse(registerResponse.trim().isEmpty());
        assertTrue(registerResponse.trim().toLowerCase().contains("success"));

        String confirmResponse = mvc.perform(get(testLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        UserEntity registeredUser = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(registeredUser);
        assertFalse(registeredUser.isValidated());
    }

    @Test
    public void givenExpiredToken_whenConfirmRegistration_thenReturnBadResponse() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";


        String registerResponse = registerUser(testEmail, testPassword);
        assertNotNull(registerResponse);
        assertFalse(registerResponse.trim().isEmpty());
        assertTrue(registerResponse.trim().toLowerCase().contains("success"));


        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        makeRegistrationTokenExpired(receivedMessages[0].getContent().toString());

        String registrationLink = getRelativeLink(receivedMessages[0].getContent().toString());
        String confirmResponse = mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(confirmResponse.toLowerCase().contains("expired"));

        UserEntity registeredUser = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(registeredUser);
        assertFalse(registeredUser.isValidated());
    }

    @Test
    public void givenTokenOfValidatedUser_whenConfirmRegistration_thenReturnBadResponse() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerUser(testEmail, testPassword);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        String registrationLink = getRelativeLink(receivedMessages[0].getContent().toString());
        mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String confirmResponse2 = mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(confirmResponse2.toLowerCase().contains("already validated"));
    }

    @Test
    public void givenTokenRenewRequest_whenRenewRegistrationToken_thenOkResponseAndSendEmail() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerUser(testEmail, testPassword);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String registrationToken = getTokenFromString(receivedMessages[0].getContent().toString());
        makeRegistrationTokenExpired(receivedMessages[0].getContent().toString());
        String renewLink = "/user/register/renew/" + registrationToken;

        String requestResponse = mvc.perform(get(renewLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("success"));

        receivedMessages = greenMail.getReceivedMessages();
        assertEquals(2, receivedMessages.length);
        assertTrue(receivedMessages[1].getContent().toString().contains("register"));

        String newRegistrationToken = getTokenFromString(receivedMessages[1].getContent().toString());

        assertNotEquals(registrationToken, newRegistrationToken);
    }

    @Test
    public void givenIncorrectRenewRequestToken_whenRenewRegistrationToken_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String requestLink = "/user/register/renew/uoiawgdiuugh";

        registerUser(testEmail, testPassword);

        String requestResponse = mvc.perform(get(requestLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("not found"));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenEmptyRenewRequestToken_whenRenewRegistrationToken_thenNotFound() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String requestLink = "/user/register/renew/";

        registerUser(testEmail, testPassword);

        String requestResponse = mvc.perform(get(requestLink))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenBlankRenewRequestToken_whenRenewRegistrationToken_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String requestLink = "/user/register/renew/       ";

        registerUser(testEmail, testPassword);

        String requestResponse = mvc.perform(get(requestLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenStillValidRequestToken_whenRenewRegistrationToken_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerUser(testEmail, testPassword);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String registrationToken = getTokenFromString(receivedMessages[0].getContent().toString());
        String renewLink = "/user/register/renew/" + registrationToken;

        String requestResponse = mvc.perform(get(renewLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("not expired"));

        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenSimpleUser_whenLogin_thenResponseOKAndGiveAuthToken() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";
        registerAndConfirmUser(testEmail, testPassword);

        String authToken = getAuthToken(testEmail, testPassword);

        assertNotNull(authToken);
        assertFalse(authToken.trim().isEmpty());
    }

    @Test
    public void givenUnconfirmedUser_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", testPassword);

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseString.toLowerCase().contains("not validated"));
    }

    @Test
    public void givenUnregisteredEmail_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";


        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", testPassword);

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseString.toLowerCase().contains("user not found"));
    }

    @Test
    public void givenInvalidEmail_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "iuagwhd");
        requestObject.put("userPassword", testPassword);

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseString.toLowerCase().contains("invalid email"));
    }

    @Test
    public void givenEmptyEmail_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "");
        requestObject.put("userPassword", testPassword);

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseString.toLowerCase().contains("invalid email"));
    }

    @Test
    public void givenBlankEmail_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "     ");
        requestObject.put("userPassword", testPassword);

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseString.toLowerCase().contains("invalid email"));
    }

    @Test
    public void givenInvalidPassword_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", "ilouahwd");

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseString.toLowerCase().contains("invalid password"));
    }

    @Test
    public void givenBlankPassword_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", "");

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseString.toLowerCase().contains("invalid password"));
    }

    @Test
    public void givenEmptyPassword_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", "     ");

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseString.toLowerCase().contains("invalid password"));
    }

    @Test
    public void givenValidAuthToken_whenGetCurrentUser_thenReturnUser() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        String authToken = getAuthToken(testEmail, testPassword);

        String responseString = mvc.perform(
                get("/user/me")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(responseString.contains(testEmail));
    }

    @Test
    public void givenInvalidAuthToken_whenGetCurrentUser_thenReturnForbidden() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        String responseString = mvc.perform(
                get("/user/me")
                        .header("Authorization", "Bearer " + "oiahwdiohawd"))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

    }

    @Test
    public void givenStandardUserAndValidEmail_whenChangeEmail_thenResponseOkAndSendEmail() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "newEmail@test.you";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("success"));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(2, receivedMessages.length);
        assertTrue(receivedMessages[1].getContent().toString().contains("changeemail"));

        String registrationTokenIDFromEmail = getTokenFromString(receivedMessages[1].getContent().toString());
        assertNotNull(registrationTokenIDFromEmail);
        assertFalse(registrationTokenIDFromEmail.trim().isEmpty());

        UserEmailChangeTokenEntity emailChangeTokenDAO = userEmailChangeTokenRepo.findByToken(registrationTokenIDFromEmail);
        assertNotNull(emailChangeTokenDAO);
        assertEquals(newEmail, emailChangeTokenDAO.getNewEmail());

        UserEntity registeredUser = userRepo.findByUserEmailIgnoreCase(testEmail);

        assertEquals(emailChangeTokenDAO.getUser().getId(), registeredUser.getId());
    }

    @Test
    public void givenStandardUserAndInvalidEmail_whenChangeEmail_thenReturnBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "iohawd";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    public void givenStandardUserAndSameEmail_whenChangeEmail_thenReturnBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", testEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    public void givenStandardUserAndTakenEmail_whenChangeEmail_thenReturnBadRequest() throws Exception {
        String testEmail1 = "email@test.me";
        String testEmail2 = "email2@test.this";
        String testPassword = "password";

        registerAndConfirmUser(testEmail1, testPassword);
        registerAndConfirmUser(testEmail2, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", testEmail2);
        String authToken = getAuthToken(testEmail1, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenStandardUserAndBlankEmail_whenChangeEmail_thenReturnBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenStandardUser_whenDeleteUser_thenOkAndMarkUserForDeletionAndLogout() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);
        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());
        String authToken = getAuthToken(testEmail,testPassword);

        String requestResponse = mvc.perform(delete("/user/deleteuser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().trim().contains("success"));


        user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(user.isToBeDeleted());

        String getLinkedUsersResponse = mvc.perform(get("/user/getlinkedusers")
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenInvalidToken_whenDeleteUser_thenForbidden() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);
        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());
        String authToken = "getAuthToken(testEmail,testPassword)";

        String requestResponse = mvc.perform(delete("/user/deleteuser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());
    }

    @Test
    public void givenBlankToken_whenDeleteUser_thenForbidden() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);
        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());
        String authToken = "";

        String requestResponse = mvc.perform(delete("/user/deleteuser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();


        user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());
    }

    @Test
    public void givenEmptyToken_whenDeleteUser_thenForbidden() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);
        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());
        String authToken = "      ";

        String requestResponse = mvc.perform(delete("/user/deleteuser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();


        user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());
    }

    @Test
    public void givenUserMarkedForDeletion_whenLogin_thenOkAndCancelDeletion() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);
        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());
        String authToken = getAuthToken(testEmail,testPassword);

        String requestResponse = mvc.perform(delete("/user/deleteuser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(user.isToBeDeleted());

        String anotherAuthToken = getAuthToken(testEmail,testPassword);

        user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertFalse(user.isToBeDeleted());

    }


    @Test
    public void givenStandardUserAndEmptyEmail_whenChangeEmail_thenReturnBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "     ";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenStandardUserAndValidEmailAndInvalidToken_whenChangeEmail_thenReturnForbidden() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "newEmail@test.this";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = "oihawoih";

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenStandardUserAndValidEmailAndBlankToken_whenChangeEmail_thenReturnForbidden() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "newEmail@test.this";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = "";

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenStandardUserAndValidEmailAndEmptyToken_whenChangeEmail_thenReturnForbidden() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "newEmail@test.this";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = "    ";

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenValidToken_whenConfirmEmailChange_thenOkResponseAndUpdateUser() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "newEmail@test.this";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("success"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(user);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(2, receivedMessages.length);
        String confirmToken = getTokenFromString(receivedMessages[1].getContent().toString());
        String confirmLink = "/user/changeemail/confirm/" + confirmToken;

        String requestResponse2 = mvc.perform(get(confirmLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse2.toLowerCase().contains("success"));

        UserEntity userWithOldEmail = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNull(userWithOldEmail);

        UserEntity userWithNewEmail = userRepo.findByUserEmailIgnoreCase(newEmail);
        assertNotNull(userWithNewEmail);
    }

    @Test
    public void givenInvalidToken_whenConfirmEmailChange_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "newEmail@test.this";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("success"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(user);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(2, receivedMessages.length);
        String confirmToken = "pioauhwdihawd";
        String confirmLink = "/user/changeemail/confirm/" + confirmToken;

        String requestResponse2 = mvc.perform(get(confirmLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        UserEntity userWithOldEmail = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(userWithOldEmail);

        UserEntity userWithNewEmail = userRepo.findByUserEmailIgnoreCase(newEmail);
        assertNull(userWithNewEmail);
    }

    @Test
    public void givenBlankToken_whenConfirmEmailChange_thenNotFound() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "newEmail@test.this";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("success"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(user);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(2, receivedMessages.length);
        String confirmToken = "";
        String confirmLink = "/user/changeemail/confirm/" + confirmToken;

        String requestResponse2 = mvc.perform(get(confirmLink))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        UserEntity userWithOldEmail = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(userWithOldEmail);

        UserEntity userWithNewEmail = userRepo.findByUserEmailIgnoreCase(newEmail);
        assertNull(userWithNewEmail);
    }

    @Test
    public void givenEmptyToken_whenConfirmEmailChange_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String newEmail = "newEmail@test.this";
        String testPassword = "password";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("newEmail", newEmail);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changeemail")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("success"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(user);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(2, receivedMessages.length);
        String confirmToken = "        ";
        String confirmLink = "/user/changeemail/confirm/" + confirmToken;

        String requestResponse2 = mvc.perform(get(confirmLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        UserEntity userWithOldEmail = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(userWithOldEmail);

        UserEntity userWithNewEmail = userRepo.findByUserEmailIgnoreCase(newEmail);
        assertNull(userWithNewEmail);
    }

    @Test
    public void givenStandardUserAndValidNewPassword_whenChangePassword_thenReturnOkAndChangePassword() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", newPassword);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("success"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertNotNull(user);
        assertFalse(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertTrue(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndInvalidOldPassword_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", "iuawhd");
        requestObject.put("newPassword", newPassword);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndBlankOldPassword_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", "");
        requestObject.put("newPassword", newPassword);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndEmptyOldPassword_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", "     ");
        requestObject.put("newPassword", newPassword);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("invalid"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndSameNewPassword_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", testPassword);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("match"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndBlankNewPassword_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", "");
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("invalid"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndEmptyNewPassword_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", "     ");
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("invalid"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndInvalidEmail_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "iouahwd");
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", newPassword);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("invalid email"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndBlankEmail_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "");
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", newPassword);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("invalid email"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenStandardUserAndEmptyEmail_whenChangePassword_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "     ");
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", newPassword);
        String authToken = getAuthToken(testEmail, testPassword);

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(requestResponse.toLowerCase().contains("invalid email"));

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenInvalidToken_whenChangePassword_thenForbidden() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", newPassword);
        String authToken = ";oiahwd";

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenBlankToken_whenChangePassword_thenForbidden() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", newPassword);
        String authToken = "";

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenEmptyToken_whenChangePassword_thenForbidden() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";
        String newPassword = "newPassword";

        registerAndConfirmUser(testEmail, testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("oldPassword", testPassword);
        requestObject.put("newPassword", newPassword);
        String authToken = "        ";

        String requestResponse = mvc.perform(post("/user/changepassword")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        UserEntity user = userRepo.findByUserEmailIgnoreCase(testEmail);
        assertTrue(passwordEncoder.matches(testPassword, user.getUserPassword()));
        assertFalse(passwordEncoder.matches(newPassword, user.getUserPassword()));
    }

    @Test
    public void givenValidUsers_whenLinkUsers_thenReturnOKAndEmailSent() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(requestResponse.toLowerCase().contains("success"));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(3, receivedMessages.length);
        assertTrue(checkMessagesForStringMatch("link", Arrays.asList(receivedMessages)));

        String linkUsersToken = getTokenFromString(
                getMessageByContentString(user1Email, "link", Arrays.asList(receivedMessages))
        );
        LinkUsersTokenEntity linkUsersTokenEntity = linkUsersTokenRepo.findByToken(linkUsersToken);
        assertNotNull(linkUsersTokenEntity);
        assertEquals(user1Email, linkUsersTokenEntity.getUserA().getUserEmail());
        assertEquals(user2Email, linkUsersTokenEntity.getUserB().getUserEmail());
    }

    @Test
    public void givenNotRegisteredEmail_whenLinkUsers_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";

        registerAndConfirmUser(user1Email, user1Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(requestResponse.toLowerCase().contains("not found"));
    }

    @Test
    public void givenMatchingEmail_whenLinkUsers_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user1Email);

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(requestResponse.toLowerCase().contains("match"));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(2, receivedMessages.length);
    }

    @Test
    public void givenNotValidatedEmail_whenLinkUsers_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(requestResponse.toLowerCase().contains("validated"));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(2, receivedMessages.length);
    }

    @Test
    public void givenInvalidEmail_whenLinkUsers_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "oiahwdolh");
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(requestResponse.toLowerCase().contains("invalid"));
    }

    @Test
    public void givenEmptyEmail_whenLinkUsers_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "");
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(requestResponse.toLowerCase().contains("invalid"));
    }

    @Test
    public void givenBlankEmail_whenLinkUsers_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "       ");
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(requestResponse.toLowerCase().contains("invalid"));
    }

    @Test
    public void givenInvalidToken_whenLinkUsers_thenForbidden() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = "oiawhd";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenEmptyToken_whenLinkUsers_thenForbidden() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = "";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenBlankToken_whenLinkUsers_thenForbidden() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = "    ";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenValidToken_whenConfirmLink_thenReturnOkAndLinkUsers() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmationLink = getRelativeLink(
                getMessageByContentString(user2Email, "link", Arrays.asList(receivedMessages))
        );

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(confirmResponse.toLowerCase().contains("success"));

        UserEntity user1 = userRepo.findByUserEmailIgnoreCase(user1Email);
        UserEntity user2 = userRepo.findByUserEmailIgnoreCase(user2Email);
        assertTrue(user1.getLinkedUsers().contains(user2.getUserEmail()));
        assertTrue(user2.getLinkedUsers().contains(user1.getUserEmail()));

    }

    @Test
    public void givenExpiredToken_whenConfirmLink_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmToken = getTokenFromString(
                getMessageByContentString(user2Email, "link", Arrays.asList(receivedMessages))
        );
        String confirmationLink = "/user/link/confirm/" + confirmToken;
        makeLinkUsersTokenExpired(confirmationLink);

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(confirmResponse.toLowerCase().contains("expired"));

        UserEntity user1 = userRepo.findByUserEmailIgnoreCase(user1Email);
        UserEntity user2 = userRepo.findByUserEmailIgnoreCase(user2Email);
        assertFalse(user1.getLinkedUsers().contains(user2.getUserEmail()));
        assertFalse(user2.getLinkedUsers().contains(user1.getUserEmail()));

    }

    @Test
    public void givenInvalidToken_whenConfirmLink_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmationLink = "/user/link/confirm/piayhwdoliohaw";

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(confirmResponse.toLowerCase().contains("not found"));

        UserEntity user1 = userRepo.findByUserEmailIgnoreCase(user1Email);
        UserEntity user2 = userRepo.findByUserEmailIgnoreCase(user2Email);
        assertFalse(user1.getLinkedUsers().contains(user2.getUserEmail()));
        assertFalse(user2.getLinkedUsers().contains(user1.getUserEmail()));
    }

    @Test
    public void givenBlankToken_whenConfirmLink_thenNotFound() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmationLink = "/user/link/confirm/";

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        UserEntity user1 = userRepo.findByUserEmailIgnoreCase(user1Email);
        UserEntity user2 = userRepo.findByUserEmailIgnoreCase(user2Email);
        assertFalse(user1.getLinkedUsers().contains(user2.getUserEmail()));
        assertFalse(user2.getLinkedUsers().contains(user1.getUserEmail()));
    }

    @Test
    public void givenEmptyToken_whenConfirmLink_thenBadRequest() throws Exception {
        String user1Email = "email@test.me";
        String user1Pasword = "password";
        String user2Email = "email2@test.you";
        String user2Pasword = "password";

        registerAndConfirmUser(user1Email, user1Pasword);
        registerAndConfirmUser(user2Email, user2Pasword);
        String authToken = getAuthToken(user1Email, user1Pasword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmationLink = "/user/link/confirm/     ";

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(confirmResponse.toLowerCase().contains("not found"));

        UserEntity user1 = userRepo.findByUserEmailIgnoreCase(user1Email);
        UserEntity user2 = userRepo.findByUserEmailIgnoreCase(user2Email);
        assertFalse(user1.getLinkedUsers().contains(user2.getUserEmail()));
        assertFalse(user2.getLinkedUsers().contains(user1.getUserEmail()));
    }

    @Test
    public void givenValidToken_whenGetLinkedUsers_thenReturnArray() throws Exception {
        String user1Email = "email@test.me";
        String password = "password";
        String user2Email = "email2@test.you";
        String user3Email = "email3@test.this";

        registerAndConfirmUser(user1Email, password);
        registerAndConfirmUser(user2Email, password);
        registerAndConfirmUser(user3Email, password);
        String authToken = getAuthToken(user1Email, password);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmToken = getTokenFromString(
                getMessageByContentString(user2Email, "link", Arrays.asList(receivedMessages))
        );
        String confirmationLink = "/user/link/confirm/" + confirmToken;

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        String getLinkedUsersResponse = mvc.perform(get("/user/getlinkedusers")
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(getLinkedUsersResponse.contains(user2Email));
        assertFalse(getLinkedUsersResponse.contains(user1Email));
        assertFalse(getLinkedUsersResponse.contains(user3Email));
    }

    @Test
    public void givenInvalidToken_whenGetLinkedUsers_thenForbidden() throws Exception {
        String user1Email = "email@test.me";
        String password = "password";
        String user2Email = "email2@test.you";
        String user3Email = "email3@test.this";

        registerAndConfirmUser(user1Email, password);
        registerAndConfirmUser(user2Email, password);
        registerAndConfirmUser(user3Email, password);
        String authToken = getAuthToken(user1Email, password);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmToken = getTokenFromString(
                getMessageByContentString(user2Email, "link", Arrays.asList(receivedMessages))
        );
        String confirmationLink = "/user/link/confirm/" + confirmToken;

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String badAuthToken = "liuahwdiuhawd";
        String getLinkedUsersResponse = mvc.perform(get("/user/getlinkedusers")
                .header("Authorization", "Bearer " + badAuthToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    public void givenBlankToken_whenGetLinkedUsers_thenForbidden() throws Exception {
        String user1Email = "email@test.me";
        String password = "password";
        String user2Email = "email2@test.you";
        String user3Email = "email3@test.this";

        registerAndConfirmUser(user1Email, password);
        registerAndConfirmUser(user2Email, password);
        registerAndConfirmUser(user3Email, password);
        String authToken = getAuthToken(user1Email, password);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmToken = getTokenFromString(
                getMessageByContentString(user2Email, "link", Arrays.asList(receivedMessages))
        );
        String confirmationLink = "/user/link/confirm/" + confirmToken;

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String badAuthToken = "";
        String getLinkedUsersResponse = mvc.perform(get("/user/getlinkedusers")
                .header("Authorization", "Bearer " + badAuthToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    public void givenEmptyToken_whenGetLinkedUsers_thenForbidden() throws Exception {
        String user1Email = "email@test.me";
        String password = "password";
        String user2Email = "email2@test.you";
        String user3Email = "email3@test.this";

        registerAndConfirmUser(user1Email, password);
        registerAndConfirmUser(user2Email, password);
        registerAndConfirmUser(user3Email, password);
        String authToken = getAuthToken(user1Email, password);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", user2Email);
        requestObject.put("userPassword", "");

        String requestResponse = mvc.perform(post("/user/link")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String confirmToken = getTokenFromString(
                getMessageByContentString(user2Email, "link", Arrays.asList(receivedMessages))
        );
        String confirmationLink = "/user/link/confirm/" + confirmToken;

        String confirmResponse = mvc.perform(get(confirmationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String badAuthToken = "      ";
        String getLinkedUsersResponse = mvc.perform(get("/user/getlinkedusers")
                .header("Authorization", "Bearer " + badAuthToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

    }

    private String getTokenFromString(String string) {
        String trimmedString = string.substring(string.lastIndexOf('/') + 1);
        return finalStringCleanup(trimmedString);
    }

    private String getRelativeLink(String link) {
        String partToRemove = "localhost:8080";
        String trimmedLink = link.replace(partToRemove, "");
        return finalStringCleanup(trimmedLink);
    }

    private String finalStringCleanup(String string) {
        return string
                .replace("\n", "")
                .replace("\r", "")
                .trim();
    }

    private boolean checkMessagesForStringMatch(String matchString, Iterable<? extends MimeMessage> messagesArray) throws IOException, MessagingException {
        for (MimeMessage message : messagesArray) {
            if (message.getContent().toString().contains(matchString)) {
                return true;
            }
        }
        return false;
    }

    private String getMessageByContentString(String email, String matchString, Iterable<? extends MimeMessage> messagesArray) throws IOException, MessagingException {
        for (MimeMessage message : messagesArray) {
            if (message.getContent().toString().contains(matchString)) {
                return message.getContent().toString();
            }
        }
        return "";
    }

    private String registerUser(String userEmail, String password) throws Exception {
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", userEmail);
        requestObject.put("userPassword", password);

        String responseString = mvc.perform(post("/user/register")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return responseString;
    }

    private void makeRegistrationTokenExpired(String registrationLink) {
        String registrationToken = getTokenFromString(registrationLink);
        UserRegistrationTokenEntity token = userRegistrationTokenRepo.findByToken(registrationToken);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.HOUR, -1);

        token.setExpiryDate(new Date(calendar.getTime().getTime()));
        userRegistrationTokenRepo.save(token);
    }

    private void makeLinkUsersTokenExpired(String linkUsersUrl) {
        String token = getTokenFromString(linkUsersUrl);
        LinkUsersTokenEntity tokenDAO = linkUsersTokenRepo.findByToken(token);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.HOUR, -1);

        tokenDAO.setExpiryDate(new Date(calendar.getTime().getTime()));
        linkUsersTokenRepo.save(tokenDAO);
    }

    private void registerAndConfirmUser(String email, String password) throws Exception {
        registerUser(email, password);
        UserEntity user = userRepo.findByUserEmailIgnoreCase(email);
        if (user == null) {
            throw new Exception();
        }

        user.setValidated(true);
        userRepo.save(user);
    }

    private String getAuthToken(String userName, String userPassword) throws Exception {
        LoginAttemptDTO loginAttemptDTO = new LoginAttemptDTO();
        loginAttemptDTO.setUserEmail(userName);
        loginAttemptDTO.setUserPassword(userPassword);

        String resultString = mvc.perform(
                post("/user/login")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(loginAttemptDTO))
                        .header("Content-Type", "application/json")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("token").toString();
    }
}
