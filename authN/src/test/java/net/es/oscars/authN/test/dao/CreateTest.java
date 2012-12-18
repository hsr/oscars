package net.es.oscars.authN.test.dao;

import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.authN.beans.Institution;
import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.beans.UserAttribute;
import net.es.oscars.authN.common.AuthNException;
import net.es.oscars.authN.dao.AttributeDAO;
import net.es.oscars.authN.dao.InstitutionDAO;
import net.es.oscars.authN.dao.UserAttributeDAO;
import net.es.oscars.authN.dao.UserDAO;
import net.es.oscars.database.hibernate.HibernateUtil;


/**
 * This class tests creation of authN database objects.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "authN", "create" })
public class CreateTest {
    private final String FIRST_NAME = "test";
    private final String LAST_NAME = "suite";
    private final String EMAIL_PRIMARY = "user@yourdomain.net";
    private final String PHONE_PRIMARY = "777-777-7777";
    private final String PHONE_SECONDARY = "888-888-8888";
    private final String STATUS = "active";
    private final String DESCRIPTION = "test user";

    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    public void setUpClass() {
        this.dbname = "testauthn";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void attributeCreate() {
        Attribute attribute = new Attribute();
        String value = CommonParams.getAttributeValue();
        String attrId = CommonParams.getAttributeId();
        attribute.setAttrId(attrId);
        attribute.setValue(value);
        attribute.setDescription("test attribute");
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        attrDAO.create(attribute);
        this.sf.getCurrentSession().getTransaction().commit();
        assert attribute.getId() != null;
        assert attribute.getValue() != null;
    }

  @Test
    public void institutionCreate() {
        Institution institution = new Institution();
        institution.setName(CommonParams.getInstitutionName());
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        institutionDAO.create(institution);
        this.sf.getCurrentSession().getTransaction().commit();
        assert institution.getId() != null;
        assert institution.getName() != null;
    }

  @Test(dependsOnMethods={ "institutionCreate" })
    public void userCreate() throws AuthNException {
        UserDAO dao = new UserDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        String ulogin = CommonParams.getLogin();
        String password = CommonParams.getPassword();
        InstitutionDAO instDAO = new InstitutionDAO(this.dbname);
        Institution inst = instDAO.queryByParam("name", CommonParams.getInstitutionName());

        User user = new User();
        user.setCertIssuer(null);
        user.setCertSubject(null);
        user.setLogin(ulogin);
        user.setLastName(LAST_NAME);
        user.setFirstName(FIRST_NAME);
        user.setEmailPrimary(EMAIL_PRIMARY);
        user.setPhonePrimary(PHONE_PRIMARY);
        user.setPhoneSecondary(PHONE_SECONDARY);
        user.setPassword(password);
        user.setDescription(DESCRIPTION);
        user.setStatus(STATUS);
        user.setInstitution(inst);
        dao.create(user);
        this.sf.getCurrentSession().getTransaction().commit();

        this.sf.getCurrentSession().beginTransaction();
        user = dao.query(ulogin);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AuthNException("Unable to query newly created user: " + ulogin);
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert user != null;
    }

  @Test(dependsOnMethods={ "attributeCreate", "userCreate" })
    public void userAttributeCreate() {
        UserDAO userDAO = new UserDAO(this.dbname);
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        String value = CommonParams.getAttributeValue();
        String login = CommonParams.getLogin();
        this.sf.getCurrentSession().beginTransaction();
        Attribute attr = (Attribute) attrDAO.queryByParam("value", value);
        User user = (User) userDAO.queryByParam("login", login);
        UserAttribute userAttr = new UserAttribute();
        userAttr.setUser(user);
        userAttr.setAttribute(attr);
        userAttrDAO.create(userAttr);
        this.sf.getCurrentSession().getTransaction().commit();
        assert userAttr.getId() != null;
    }
}
