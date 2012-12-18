package net.es.oscars.database.hibernate;

import java.util.*;
import java.lang.reflect.ParameterizedType;
import java.io.Serializable;

import org.hibernate.*;
import org.hibernate.criterion.*;

/**
 * GenericHibernateDAO, an abstract class, is adapted from section 16.2.2 of
 * Java Persistence with Hibernate.  It is subclassed by all OSCARS
 * DAO classes.
 *
 * @author christian(at)hibernate.org
 * @author dwrobertson@lbl.gov
 */
public abstract class GenericHibernateDAO<T, ID extends Serializable>
        implements GenericDAO<T, ID> {

    private Class<T> persistentClass;
    private String dbName;

    public GenericHibernateDAO() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                                .getGenericSuperclass())
                                .getActualTypeArguments()[0];
    }

    public void setDatabase(String dbName) {
        this.dbName = dbName;
    }

    protected Session getSession() {
        return HibernateUtil.getSessionFactory(this.dbName).getCurrentSession();
    }

    public Class<T> getPersistentClass() { return persistentClass; }

  @SuppressWarnings("unchecked")
    public T findById(ID id, boolean lock) {
        T entity;
        if (lock)
            entity = (T)
                getSession().get(getPersistentClass(), id, LockMode.UPGRADE);
        else
            entity = (T) getSession().get(getPersistentClass(), id);

        return entity;
    }

  @SuppressWarnings("unchecked")
    public List<T> list() { return findByCriteria(); }

  @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String[] excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example =  Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }

  @SuppressWarnings("unchecked")
    public void create(T entity) {
        getSession().saveOrUpdate(entity);
    }

  @SuppressWarnings("unchecked")
    public void update(T entity) {
        getSession().saveOrUpdate(entity);
    }

    public void remove(T entity) { getSession().delete(entity); }
    public void flush() { getSession().flush(); }
    public void clear() { getSession().clear(); }

   /**
    * Finds unique row based on query with parameter name and value.
    *     Parameter name must also be a column name for this to work.
    *     NOTE:  This will not work with dates.
    *
    * @param paramName A String with parameter name
    * @param paramValue An Object with parameter value
    * @return An instance T of the associated persistent class.
    */
  @SuppressWarnings("unchecked")
    public T queryByParam(String paramName, Object paramValue) {
        String hsql = "from " + this.persistentClass.getName() +
                                " where " + paramName + " = :" + paramName;
        return (T) getSession().createQuery(hsql)
                               .setParameter(paramName, paramValue)
                               .setMaxResults(1)
                               .uniqueResult();
    }

    /**
     * Use this inside subclasses as convenience method.
     */
  @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return crit.list();
   }
}
