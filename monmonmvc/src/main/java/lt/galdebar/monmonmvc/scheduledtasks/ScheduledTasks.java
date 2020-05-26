package lt.galdebar.monmonmvc.scheduledtasks;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.service.ShoppingItemService;
import lt.galdebar.monmonmvc.service.UserService;
import lt.galdebar.monmonmvc.service.exceptions.shoppingitem.ShoppingItemNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Runs periodic scheduled tasks.
 */
@Component
@Log4j2
public class ScheduledTasks {

    @Autowired
    private UserService userService;

    @Autowired
    private ShoppingItemService shoppingItemService;

    /**
     * Delete expired users. Executes on a timer set in the application.properties with task.schedule.period property.<br>
     *     Gathers all users pending deletion (toBeDeleted=true and Grace Period past)
     *     For each user pending deletion:
     *     <ul>
     *         <li>Find all linked users and remove the pending deletion user</li>
     *         <li>Find all shopping items belonging to the user and remove the pending user from the list</li>
     *         <li>Call for final user deletion</li>
     *     </ul>
     */
    @Scheduled(cron = "${task.schedule.period}")
    public void deleteExpiredUsers() {
        log.info("Scheduled task. Deleting users pending removal. ");
        List<UserDTO> usersToDelete = userService.getUsersPendingDeletion();

        for (UserDTO userDTO : usersToDelete) {
            removeUsersFromShoppingItems(userDTO);
            unlinkUsers(userDTO);
        }

        userService.deleteUsers(usersToDelete);
    }

    @Transactional
    private void unlinkUsers(UserDTO userDTO) {
        for (String userBEmail : userDTO.getLinkedUsers()) {
            UserDTO tempUser = new UserDTO();
            tempUser.setUserEmail(userBEmail);
            userService.unlinkUsers(userDTO, tempUser);
        }
    }

    @Transactional
    private void removeUsersFromShoppingItems(UserDTO userDTO) {
        Set<ShoppingItemDTO> allShoppingItems = new HashSet<>(
                shoppingItemService.getAll(userDTO)
        );
        List<ShoppingItemDTO> shoppingItemsToDelete = new ArrayList<>();
        List<ShoppingItemDTO> shoppingItemsToUpdate = new ArrayList<>();

        for (ShoppingItemDTO item : allShoppingItems) {
            item.getUsers().remove(userDTO.getUserEmail());
            if (item.getUsers().size() == 0) {
                shoppingItemsToDelete.add(item);
            } else shoppingItemsToUpdate.add(item);
        }

        try {
            shoppingItemService.updateItems(shoppingItemsToUpdate);
            shoppingItemService.deleteItems(shoppingItemsToDelete);
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            log.warn("Could not update shopping item after removing user. ");
        }
    }

}
