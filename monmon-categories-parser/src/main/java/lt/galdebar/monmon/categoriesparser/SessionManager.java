package lt.galdebar.monmon.categoriesparser;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private SessionFactory sessionFactory;

    public SessionManager() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public SessionManager(Configuration configuration) {
        sessionFactory = configuration.buildSessionFactory();
    }


    public void closeConnection() {
        sessionFactory.close();
    }


    public <T> void pushList(List<T> objects) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        for (T t : objects) {
            session.save(t);

        }

        session.getTransaction().commit();
        session.close();
    }
}
