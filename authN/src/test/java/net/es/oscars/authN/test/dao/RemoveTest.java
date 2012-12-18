package net.es.oscars.authN.test.dao;

import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.authN.beans.Institution;
import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.beans.UserAttribute;
import net.es.oscars.authN.dao.AttributeDAO;
import net.es.oscars.authN.dao.InstitutionDAO;
import net.es.oscars.authN.dao.UserAttributeDAO;
import net.es.oscars.authN.dao.UserDAO;
import net.es.oscars.database.hibernate.HibernateUtil;


/**
 * This class tests removal of authN database entries.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authN" }, dependsOnGroups={ "institution", "user", "attribute" })
public class RemoveTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    public void setUpClass() {
        this.dbname = "testauthn";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void userAttributeRemove() {
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        UserDAO userDAO = new UserDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        User user = (User) userDAO.queryByParam("login",
                                             CommonParams.getLogin());
        // remove a user attribute
        UserAttribute userAttr =
             (UserAttribute) userAttrDAO.queryByParam("userId", user.getId());
        userAttrDAO.remove(userAttr);
        userAttr = (UserAttribute) userAttrDAO.queryByParam("userId",
                                                            user.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert userAttr == null;
    }

  @Test(dependsOnMethods={ "userAttributeRemove" })
    public void attributeRemove() {
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        // remove an attribute
        Attribute attr = (Attribute) attrDAO.queryByParam("value",
                                   CommonParams.getAttributeValue());
        attrDAO.remove(attr);
        attr = (Attribute) attrDAO.queryByParam("value",
                                   CommonParams.getAttributeValue());
        this.sf.getCurrentSession().getTransaction().commit();
        assert attr == null;
    }

  @Test(dependsOnMethods={ "userAttributeRemove" })
    public void userRemove() {
        UserDAO dao = new UserDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        User user = dao.query(CommonParams.getLogin());
        dao.remove(user);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "userRemove" })
    public void institutionRemove() {
        this.sf.getCurrentSession().beginTransaction();
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        // remove one of the institutions
        Institution institution =
                (Institution) institutionDAO.queryByParam("name",
                                   CommonParams.getInstitutionName());
        institutionDAO.remove(institution);
        institution = (Institution) institutionDAO.queryByParam("name",
                                   CommonParams.getInstitutionName());
        this.sf.getCurrentSession().getTransaction().commit();
        assert institution == null;
    }
}
