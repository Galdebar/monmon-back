package lt.galdebar.monmonapi.app.scheduledtasks;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemEntity;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingListRepo;
import lt.galdebar.monmonapi.app.services.shoppinglists.ShoppingListService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
class ScheduleListDeleteTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ShoppingListRepo listRepo;

    @Autowired
    private ShoppingItemRepo itemRepo;

    @Autowired
    private ShoppingListService listService;

    @Autowired
    private ScheduleListDelete scheduleListDelete;

    @BeforeEach
    void setup() {
        itemRepo.deleteAll();
        listRepo.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        itemRepo.deleteAll();
        listRepo.deleteAll();
    }

    @Test
    void whenDeleteLists_thenDeleteOldAndPendingDeletionListsAndItems() {
        String pendingDeleteListName = "pendingList";
        String oldListName = "oldList";
        String remainingListName = "remainingList";
        String password = "somePassword";

        ShoppingListEntity pendingDeleteList = createAndSaveList(pendingDeleteListName, password);
        ShoppingListEntity oldList = createAndSaveList(oldListName, password);
        ShoppingListEntity remainingList = createAndSaveList(remainingListName, password);

        pendingDeleteList.setPendingDeletion(true);
        pendingDeleteList.setDeletionTime(now().minusDays(1));

        oldList.setLastUsedTime(now().minus(scheduleListDelete.getINACTIVE_LIST_GRACE_PERIOD_DAYS() * 2, ChronoUnit.DAYS));

        String testItemName = "testItem";
        ShoppingItemEntity pendingDeleteItem = new ShoppingItemEntity();
        pendingDeleteItem.setItemName(testItemName);
        pendingDeleteItem.setShoppingList(pendingDeleteList);

        ShoppingItemEntity oldItem = new ShoppingItemEntity();
        oldItem.setItemName(testItemName);
        oldItem.setShoppingList(oldList);

        ShoppingItemEntity remainingItem = new ShoppingItemEntity();
        remainingItem.setItemName(testItemName);
        remainingItem.setShoppingList(remainingList);

        listRepo.save(pendingDeleteList);
        listRepo.save(oldList);
        itemRepo.save(pendingDeleteItem);
        itemRepo.save(oldItem);
        itemRepo.save(remainingItem);

        scheduleListDelete.deleteLists();

        assertEquals(1, listRepo.count());
        assertTrue(listRepo.findById(pendingDeleteList.getId()).isEmpty());
        assertTrue(listRepo.findById(oldList.getId()).isEmpty());
        assertFalse(listRepo.findById(remainingList.getId()).isEmpty());

        assertEquals(1, itemRepo.count());
        assertEquals(1, itemRepo.findByShoppingList(remainingList).size());


    }

    private ShoppingListEntity createAndSaveList(String name, String password) {
        ShoppingListEntity entity = new ShoppingListEntity();
        entity.setTimeCreated(now());
        entity.setLastUsedTime(now());
        entity.setPendingDeletion(false);
        entity.setName(name);
        entity.setPassword(passwordEncoder.encode(password));
        return listRepo.save(entity);
    }
}