package lt.galdebar.monmonmvc.service;


import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.dao.InMemoryItemRepo;
import lt.galdebar.monmonmvc.dao.MongoDBRepo;
import lt.galdebar.monmonmvc.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {
    @Autowired
    @Qualifier("inMemoryRepo")
    private final InMemoryItemRepo inMemoryItemRepo;

    @Autowired
    private final MongoDBRepo mongoDBRepo;

    public void addItem(Item item){
        inMemoryItemRepo.insertItem(item);
        mongoDBRepo.insert(item);
    }

    public Item getItemByName(String itemName){
        return inMemoryItemRepo.getItemByName(itemName);
    }
}
