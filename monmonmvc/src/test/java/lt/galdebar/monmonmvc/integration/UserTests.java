package lt.galdebar.monmonmvc.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.EmailSenderService;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
public class UserTests {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailSenderService emailSenderService;

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

    }

    //

    @AfterEach
    public void tearDown(){
        userRepo.deleteAll();
    }
}
