package net.es.oscars.database.hibernate;

import java.io.Serializable;
import java.util.*;

/**
 * GenericDAO is the interface implemented by GenericHibernateDAO.
 *
 * @author christian(at)hibernate.org
 */
public interface GenericDAO<T, ID extends Serializable> {

    T findById(ID id, boolean lock);

    List<T> list();

    List<T> findByExample(T exampleInstance, String[] excludeProperty);

    void create(T entity);

    void update(T entity);

    void remove(T entity);
}
