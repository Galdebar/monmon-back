package lt.galdebar.monmonmvc.dao;

import lt.galdebar.monmonmvc.model.category.NewCategoryDAO;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewCategoriesRepository extends CrudRepository<NewCategoryDAO, Long> {
}
