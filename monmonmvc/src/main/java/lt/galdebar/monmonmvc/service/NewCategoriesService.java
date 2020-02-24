package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.repositories.NewCategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class NewCategoriesService {

    @Autowired
    private NewCategoriesRepository repository;

    public String search(String searchString){
        return "";
    }
}
