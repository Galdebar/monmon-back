package lt.galdebar.monmonmvc.integration;

import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingItemEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import lt.galdebar.monmonmvc.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest(properties = {"task.schedule.period=*/8 * * * * *"})
public class ScheduledTasksTests {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ShoppingItemRepo shoppingItemRepo;

    @Before
    public void setUp() {
        userRepo.deleteAll();
        shoppingItemRepo.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        userRepo.deleteAll();
        shoppingItemRepo.deleteAll();
    }

    @Test
    public void givenUserPendingDeletion_whenAwaitScheduledTask_thenUserAndItemsDeleted() {
        String testEmail = "test@somemail.com";

        createAndSaveUser(testEmail, true);

        await().atMost(10, TimeUnit.SECONDS).until(() -> userRepo.findByUserEmailIgnoreCase(testEmail) == null);
        assertNull(userRepo.findByUserEmailIgnoreCase(testEmail));
    }

    @Test
    public void givenShoppingItem_whenAwaitForScheduledTask_thenShoppingItemRemoved() {
        String testEmail = "test@somemail.com";
        String testItemName = "Beer";

        createAndSaveUser(testEmail, true);
        createShoppingItem(testItemName, testEmail);

        await().atMost(10, TimeUnit.SECONDS).until(() -> userRepo.findByUserEmailIgnoreCase(testEmail) == null);
        assertNull(shoppingItemRepo.findByItemName(testItemName));
    }

    @Test
    public void givenSeveralUsersAndItems_whenAwaitScheduledTask_thenRemoveOnlyPendingUserAndTheirTrace() {
        String testEmail1 = "test@somemail.com";
        String testEmail2 = "test@othermail.com";

        createAndSaveUser(testEmail1, false);
        createAndSaveUser(testEmail2, true);

        await().atMost(10, TimeUnit.SECONDS).until(() -> userRepo.findByUserEmailIgnoreCase(testEmail2) == null);
        assertEquals(1, userRepo.findAll().size());
        assertNotNull(userRepo.findByUserEmailIgnoreCase(testEmail1));
        assertNull(userRepo.findByUserEmailIgnoreCase(testEmail2));

    }

    @Test
    public void givenSeveralUsersAndShoppingItems_whenAwaitScheduledTask_thenRemoveOnlyUserItems() {
        String testEmail1 = "test@somemail.com";
        String testEmail2 = "test@othermail.com";
        String remainingItemName = "This item should remain";
        String itemToBeDeleted = "This item should be deleted";

        createAndSaveUser(testEmail1, false);
        createAndSaveUser(testEmail2, true);
        createShoppingItem(remainingItemName, testEmail1);
        createShoppingItem(itemToBeDeleted, testEmail2);


        await().atMost(10, TimeUnit.SECONDS).until(() -> userRepo.findByUserEmailIgnoreCase(testEmail2) == null);

        assertNotNull(shoppingItemRepo.findByItemName(remainingItemName));
        assertNull(shoppingItemRepo.findByItemName(itemToBeDeleted));

    }

    @Test
    public void givenLinkedUsers_whenAwaitScheduledTask_thenRemoveUserAndTraceInItems() {
        String testEmail1 = "test@somemail.com";
        String testEmail2 = "test@othermail.com";
        String remainingItemName = "This item should remain";

        UserEntity userA = createAndSaveUser(testEmail1, false);
        UserEntity userB = createAndSaveUser(testEmail2, true);
        userA.getLinkedUsers().add(userB.getUserEmail());
        userB.getLinkedUsers().add(userA.getUserEmail());
        userRepo.save(userA);
        userRepo.save(userB);
        ShoppingItemEntity item = createShoppingItem(remainingItemName, testEmail1);
        item.users.add(testEmail2);
        shoppingItemRepo.save(item);


        await().atMost(10, TimeUnit.SECONDS).until(() -> userRepo.findByUserEmailIgnoreCase(testEmail2) == null);

        ShoppingItemEntity shoppingItem = shoppingItemRepo.findByItemName(remainingItemName);
        assertNotNull(shoppingItem);
        assertEquals(1, shoppingItem.users.size());
        assertEquals(testEmail1,shoppingItem.users.toArray()[0]);
    }

    private UserEntity createAndSaveUser(String email, boolean isToBeDeleted) {
        UserEntity user = new UserEntity();
        user.setUserEmail(email);
        user.setValidated(true);
        user.setToBeDeleted(isToBeDeleted);
        user.setDeletionDate(new Date());
        return userRepo.save(user);
    }

    private ShoppingItemEntity createShoppingItem(String itemName, String userEmail) {
        ShoppingItemEntity shoppingItem = new ShoppingItemEntity();
        shoppingItem.itemName(itemName);
        shoppingItem.users.add(userEmail);
        return shoppingItemRepo.save(shoppingItem);
    }

}