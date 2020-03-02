package lt.galdebar.monmonmvc.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShoppingItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void getItemsByCategory() {

    }

    @Test
    void getAllItems() {
    }

    @Test
    void addItem() {
    }

    @Test
    void updateItem() {
    }

    @Test
    void updateItems() {
    }

    @Test
    void deleteById() {
    }

    @Test
    void deleteItems() {
    }
}