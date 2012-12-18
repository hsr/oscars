package net.es.oscars.authN.test.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.beans.UserAttribute;
import net.es.oscars.authN.common.AuthNException;
import net.es.oscars.authN.dao.AttributeDAO;
import net.es.oscars.authN.dao.UserAttributeDAO;
import net.es.oscars.authN.dao.UserDAO;
import net.es.oscars.database.hibernate.HibernateUtil;

/**
 * This class tests methods accessing the userAttributes table, which requires
 * a working UserAttribute.java and UserAttribute.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authN", "userAttribute" }, dependsOnGroups={ "create" })
public class UserAttributeTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "testauthn";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void getAttributesByUser() {
        UserDAO userDAO = new UserDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        User user =
                (User) userDAO.queryByParam("login",
                                            CommonParams.getLogin());
        List<UserAttribute> userAttrs =
            userAttrDAO.getAttributesByUser(user.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert !userAttrs.isEmpty();
    }

  @Test
    public void getUsersByAttribute() throws AuthNException {
        AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        List<User> users = new ArrayList<User>();
        this.sf.getCurrentSession().beginTransaction();
        Attribute attribute =
                (Attribute) attributeDAO.queryByParam("value",
                                    CommonParams.getAttributeValue());
        try {
            users =
                userAttrDAO.getUsersByAttribute(attribute.getValue());
        } catch (AuthNException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !users.isEmpty();
    }
}
