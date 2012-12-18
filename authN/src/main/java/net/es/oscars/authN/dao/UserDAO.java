package net.es.oscars.authN.dao;

import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authN.beans.User;

/**
 * UserDAO is the data access object for the authN.users table.
 * Some methods in this class require authorization.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class UserDAO extends GenericHibernateDAO<User, Integer> {

    public UserDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /**
     * Finds a system user based on their login name.
     *
     * @param login A user's login name
     * @return user The corresponding user instance, if any
     */
    public User query(String login) {
        User user = (User) this.queryByParam("login", login);
        return user;
    }

    /**
     * Retrieves a list of users in alphabetical order.
     *
     * @return list of users
     */
    public List<User> list() {
        String sql = "select * from users " +
                     "order by login";
        List<User> users =
               (List<User>) this.getSession().createSQLQuery(sql)
                                             .addEntity(User.class)
                                             .list();
        return users;
    }

    /**
     * Makes sure user corresponding to DN has an entry in the db.
     *
     * @param certSubject A string containing the subject from the certificate
     * @return user the associated user instance, if any.
     */
    public User fromDN(String certSubject) {
        User user = (User) this.queryByParam("certSubject", certSubject);
        return user;
    }

    /**
     * Checks session cookie for validity.
     *
     * @param userName string with user's login name
     * @param sessionName string with session cookie value
     * @return boolean indicating whether cookie is valid
     */
    public boolean validSession(String userName, String sessionName) {

        String hsql = "from User " +
            "where login = ? and cookieHash = ?";
        User user = (User) this.getSession().createQuery(hsql)
                                            .setString(0, userName)
                                            .setString(1, sessionName)
                                            .setMaxResults(1)
                                            .uniqueResult();
        return user != null ? true : false;
    }
}
