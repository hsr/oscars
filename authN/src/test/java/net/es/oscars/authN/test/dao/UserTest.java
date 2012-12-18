package net.es.oscars.authN.test.dao;


import java.util.List;

import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.common.AuthNException;
import net.es.oscars.authN.dao.UserDAO;
import net.es.oscars.database.hibernate.HibernateUtil;

/**
 * This class tests methods in UserDAO.java via UserManager.java, which
 * requires a working User.java and User.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authN", "user" }, dependsOnGroups={ "create" })
public class UserTest {
    private final String PHONE_SECONDARY = "888-888-8888";
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "testauthn";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void userAssociation() throws AuthNException {
        User user = null;

        UserDAO dao = new UserDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        String ulogin = CommonParams.getLogin();
        user = dao.query(ulogin);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthNException("Unable to query newly created user: " + ulogin);
        }
        String institutionName = user.getInstitution().getName();
        this.sf.getCurrentSession().getTransaction().commit();
        assert institutionName != null;
    }

  @Test
    public void userQuery() throws AuthNException {
        User user = null;
        String ulogin = CommonParams.getLogin();

        UserDAO dao = new UserDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        user = dao.query(ulogin);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthNException("Unable to query newly created user: " + ulogin);
        }
        String userLogin = user.getLogin();
        this.sf.getCurrentSession().getTransaction().commit();
        assert ulogin.equals(userLogin);
    }

  @Test
      public void userList() {
          UserDAO dao = new UserDAO(this.dbname);
          this.sf.getCurrentSession().beginTransaction();
          List<User> users = dao.list();
          this.sf.getCurrentSession().getTransaction().commit();
          assert !users.isEmpty();
      }

  @Test
      public void userUpdate() throws AuthNException {
          UserDAO dao = new UserDAO(this.dbname);
          this.sf.getCurrentSession().beginTransaction();
          String ulogin = CommonParams.getLogin();
          User user = dao.query(ulogin);
          if (user == null) {
              this.sf.getCurrentSession().getTransaction().rollback();
              throw new AuthNException("Unable to get user instance in order to update: " + ulogin);
        }
        user.setPhoneSecondary(PHONE_SECONDARY);
        dao.update(user);
        user = dao.query(ulogin);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthNException("Unable to query newly updated user: " + ulogin);
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert user.getPhoneSecondary().equals(PHONE_SECONDARY) :
            "Updated secondary phone number, " + user.getPhoneSecondary() +
            " does not equal desired phone number, " + PHONE_SECONDARY;
    }
}
