package lt.galdebar.monmonmvc.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.UserRegistrationTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.LoginAttemptDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRegistrationTokenRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.EmailSenderService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext wac;


    private MockMvc mvc;
    private ObjectMapper objectMapper;

    @Before
    public void setup(){
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
    }


    @After
    public void afterEach() {
        userRepo.deleteAll();
        userRegistrationTokenRepo.deleteAll();
        greenMail.stop();
    }

    @Test
    public void givenContext_whenSendEmail_thenReceiveEmail() throws MessagingException, IOException {
        String testEmail = "test@email.com";
        String testToken = "iauwhdiuhawd";

        emailSenderService.sendUserConnectConfirmationEmail(testEmail, testToken);

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
        UserRegistrationTokenDAO registrationToken;
        UserDAO registeredUser;
        MimeMessage[] receivedMessages;


        registerResponse = registerUser(testEmail, testPassword);
        assertNotNull(registerResponse);
        assertFalse(registerResponse.trim().isEmpty());
        assertEquals("Success", registerResponse);

        receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
        assertTrue(receivedMessages[0].getContent().toString().contains("register"));

        registrationTokenIDFromEmail = getTokenFromString(receivedMessages[0].getContent().toString());
        assertNotNull(registrationTokenIDFromEmail);
        assertFalse(registrationTokenIDFromEmail.trim().isEmpty());

        registrationToken = userRegistrationTokenRepo.findByToken(registrationTokenIDFromEmail);
        assertNotNull(registrationToken);

        registeredUser = userRepo.findByUserEmail(testEmail);
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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

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
        assertEquals("Success", registerResponse);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
        assertTrue(receivedMessages[0].getContent().toString().contains("register"));
        String registrationLink = getRegistrationLink(receivedMessages[0].getContent().toString());

        String confirmResponse = mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        UserDAO registeredUser = userRepo.findByUserEmail(testEmail);
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
        assertEquals("Success", registerResponse);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
        assertTrue(receivedMessages[0].getContent().toString().contains("register"));
        String registrationLink = getRegistrationLink(receivedMessages[0].getContent().toString());
        registrationLink += "iahwikdhaw";

        String confirmResponse = mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        UserDAO registeredUser = userRepo.findByUserEmail(testEmail);
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
        assertEquals("Success", registerResponse);

        String confirmResponse = mvc.perform(get(testLink))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        UserDAO registeredUser = userRepo.findByUserEmail(testEmail);
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
        assertEquals("Success", registerResponse);

        String confirmResponse = mvc.perform(get(testLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        UserDAO registeredUser = userRepo.findByUserEmail(testEmail);
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
        assertEquals("Success", registerResponse);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        makeTokenExpired(receivedMessages[0].getContent().toString());

        String registrationLink = getRegistrationLink(receivedMessages[0].getContent().toString());
        String confirmResponse = mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(confirmResponse.toLowerCase().contains("expired"));

        UserDAO registeredUser = userRepo.findByUserEmail(testEmail);
        assertNotNull(registeredUser);
        assertFalse(registeredUser.isValidated());
    }

    @Test
    public void givenTokenOfValidatedUser_whenConfirmRegistration_thenReturnBadResponse() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerUser(testEmail, testPassword);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        String registrationLink = getRegistrationLink(receivedMessages[0].getContent().toString());
        mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String confirmResponse2 = mvc.perform(get(registrationLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(confirmResponse2.toLowerCase().contains("already validated"));
    }

    @Test
    public void givenTokenRenewRequest_whenRenewRegistrationToken_thenOkResponseAndSendEmail() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerUser(testEmail, testPassword);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String registrationToken = getTokenFromString(receivedMessages[0].getContent().toString());
        makeTokenExpired(receivedMessages[0].getContent().toString());
        String renewLink = "/user/register/renew/" + registrationToken;

        String requestResponse = mvc.perform(get(renewLink))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(requestResponse.toLowerCase().contains("incorrect"));

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenStillValidRequestToken_whenRenewRegistrationToken_thenBadRequest() throws Exception {
        String testEmail = "email@test.me";
        String testPassword = "password";

        registerUser(testEmail,testPassword);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        String registrationToken = getTokenFromString(receivedMessages[0].getContent().toString());
        String renewLink = "/user/register/renew/" + registrationToken;

        String requestResponse = mvc.perform(get(renewLink))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(requestResponse.toLowerCase().contains("not expired"));

        assertEquals(1, receivedMessages.length);
    }

    @Test
    public void givenSimpleUser_whenLogin_thenResponseOKAndGiveAuthToken() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";
        registerAndConfirmUser(testEmail,testPassword);

        String authToken = getAuthToken(testEmail,testPassword);

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseString.toLowerCase().contains("user not found"));
    }

    @Test
    public void givenInvalidEmail_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail,testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "iuagwhd");
        requestObject.put("userPassword", testPassword);

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseString.toLowerCase().contains("invalid email"));
    }

    @Test
    public void givenEmptyEmail_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail,testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "");
        requestObject.put("userPassword", testPassword);

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseString.toLowerCase().contains("invalid email"));
    }

    @Test
    public void givenBlankEmail_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail,testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", "     ");
        requestObject.put("userPassword", testPassword);

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseString.toLowerCase().contains("invalid email"));
    }

    @Test
    public void givenInvalidPassword_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail,testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", "ilouahwd");

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseString.toLowerCase().contains("invalid password"));
    }

    @Test
    public void givenBlankPassword_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail,testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", "");

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseString.toLowerCase().contains("invalid password"));
    }

    @Test
    public void givenEmptyPassword_whenLogin_thenBadRequest() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail,testPassword);

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("userEmail", testEmail);
        requestObject.put("userPassword", "     ");

        String responseString = mvc.perform(post("/user/login")
                .characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseString.toLowerCase().contains("invalid password"));
    }

    @Test
    public void givenValidAuthToken_whenGetCurrentUser_thenReturnUser() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail,testPassword);

        String authToken = getAuthToken(testEmail,testPassword);

        String responseString = mvc.perform(
                get("/user/me")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseString.contains(testEmail));
    }

    @Test
    public void givenInvalidAuthToken_whenGetCurrentUser_thenReturnForbidden() throws Exception {
        String testEmail = "test@email.com";
        String testPassword = "password";

        registerAndConfirmUser(testEmail,testPassword);

        String responseString = mvc.perform(
                get("/user/me")
                        .header("Authorization", "Bearer " + "oiahwdiohawd"))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

    }

    //get current user
    //error if token incorrect

    //error if not loged in
    //change email
    //error if not logged in or incorrect auth token
    //error if incorrect email format

    //error if email already taken
    //change email confirm
    //error if incorrect token

    //error if token expired
    //change password
    //error if pass the same
    //error if passwords don't match (?)
    //error if email invalid

    //error if incorrect auth token
    //link users
    //error if incorrect token
    // error if emails match
    // error if email invalid
    // error if user not found

    //error if user not validated
    //confirm user link
    // error if incorrect token

    //error if token expired
    //get linked users
    // get linked users works both ways

    // error when incorrect auth token
    private String getTokenFromString(String string) {
        String trimmedString = string.substring(string.lastIndexOf('/') + 1);
        return finalStringCleanup(trimmedString);
    }

    private String getRegistrationLink(String link) {
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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return responseString;
    }

    private void makeTokenExpired(String registrationLink) {
        String registrationToken = getTokenFromString(registrationLink);
        UserRegistrationTokenDAO token = userRegistrationTokenRepo.findByToken(registrationToken);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.HOUR, -1);

        token.setExpiryDate(new Date(calendar.getTime().getTime()));
        userRegistrationTokenRepo.save(token);
    }

    private void registerAndConfirmUser(String email, String password) throws Exception {
        registerUser(email,password);
        UserDAO user = userRepo.findByUserEmail(email);
        if(user == null){ throw new Exception();}

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
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("token").toString();
    }
}
