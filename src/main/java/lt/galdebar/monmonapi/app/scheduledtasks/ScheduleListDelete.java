package lt.galdebar.monmonapi.app.scheduledtasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListDTO;
import lt.galdebar.monmonapi.app.services.shoppingitems.ShoppingItemService;
import lt.galdebar.monmonapi.app.services.shoppinglists.ShoppingListService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Log4j2
@RequiredArgsConstructor
public class ScheduleListDelete {
    private final ShoppingListService listService;
    private final ShoppingItemService itemService;
    @Getter
    private final int INACTIVE_LIST_GRACE_PERIOD_DAYS = 90;


    @Scheduled(cron = "${task.schedule.period}")
    public void deleteLists() {
        log.info("Scheduled task - Shopping list deletion");
        deletePendingLists();
        deleteOldLists();
    }

    private void deletePendingLists() {
        log.info("Scanning lists pending deletion.");
        List<ShoppingListDTO> pendingLists = listService.getListsPendingDeletion();
        pendingLists.forEach(this::deleteListAndItems);
    }

    private void deleteOldLists() {
        log.info("Scanning lists that haven't been used in " + INACTIVE_LIST_GRACE_PERIOD_DAYS + " days");
        List<ShoppingListDTO> oldLists = listService.getOldLists(LocalDateTime.now().minus(INACTIVE_LIST_GRACE_PERIOD_DAYS, ChronoUnit.DAYS));
        oldLists.forEach(this::deleteListAndItems);

    }

    private void deleteListAndItems(ShoppingListDTO shoppingListDTO) {
        itemService.deleteAllItems(
                listService.findByListName(shoppingListDTO.getName())
        );
        listService.deleteList(shoppingListDTO.getName());
    }

}
