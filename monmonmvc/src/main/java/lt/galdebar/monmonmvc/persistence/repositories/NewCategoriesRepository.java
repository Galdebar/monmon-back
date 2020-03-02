package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingCategoryDAO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewCategoriesRepository extends CrudRepository<ShoppingCategoryDAO, Long> {
}
