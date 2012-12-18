package net.es.oscars.authN.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

public class User extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** persistent field */
    private String login;

    /** nullable persistent field */
    private String certIssuer;

    /** nullable persistent field */
    private String certSubject;

    /** persistent field */
    private String lastName;

    /** persistent field */
    private String firstName;

    /** persistent field */
    private String emailPrimary;

    /** persistent field */
    private String phonePrimary;

    /** nullable persistent field */
    private String password;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String emailSecondary;

    /** nullable persistent field */
    private String phoneSecondary;

    /** nullable persistent field */
    private String status;

    /** nullable persistent field */
    private String activationKey;

    /** nullable persistent field */
    private Long loginTime;

    /** nullable persistent field */
    private String cookieHash;

    /** persistent field */
    private Institution institution;

    /** default constructor */
    public User() { }

    /**
     * @return login A String with the user login name
     */ 
    public String getLogin() { return this.login; }

    /**
     * @param login A String with the user login name
     */ 
    public void setLogin(String login) { this.login = login; }


    /**
     * @return certIssuer A String with the certificate issuer
     */ 
    public String getCertIssuer() { return this.certIssuer; }

    /**
     * @param certIssuer A String with the certIssuer name
     */ 
    public void setCertIssuer(String certIssuer) {
        this.certIssuer = certIssuer;
    }


    /**
     * @return certSubject A String with the certificate subject
     */ 
    public String getCertSubject() { return this.certSubject; }

    /**
     * @param certSubject A String with the certificate subject
     */ 
    public void setCertSubject(String certSubject) {
        this.certSubject = certSubject;
    }


    /**
     * @return lastName A String with the user's last name
     */ 
    public String getLastName() { return this.lastName; }

    /**
     * @param lastName A String with the user's last name
     */ 
    public void setLastName(String lastName) { this.lastName = lastName; }


    /**
     * @return firstName A String with the user's first name
     */ 
    public String getFirstName() { return this.firstName; }

    /**
     * @param firstName A String with the user's first name
     */ 
    public void setFirstName(String firstName) { this.firstName = firstName; }


    /**
     * @return emailPrimary A String with the user's primary email address
     */ 
    public String getEmailPrimary() { return this.emailPrimary; }

    /**
     * @param emailPrimary A String with the user's primary email address
     */ 
    public void setEmailPrimary(String emailPrimary) {
        this.emailPrimary = emailPrimary;
    }


    /**
     * @return phonePrimary A String with the user's primary phone number
     */ 
    public String getPhonePrimary() { return this.phonePrimary; }

    /**
     * @param phonePrimary A String with the user's primary phone number
     */ 
    public void setPhonePrimary(String phonePrimary) {
        this.phonePrimary = phonePrimary;
    }


    /**
     * @return password A String with the user's password
     */ 
    public String getPassword() { return this.password; }

    /**
     * @param password A String with the user's password
     */ 
    public void setPassword(String password) { this.password = password; }


    /**
     * @return description A String with a description of the user
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description A String with a description of the user
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return emailSecondary A String with the user's secondary email address
     */ 
    public String getEmailSecondary() { return this.emailSecondary; }

    /**
     * @param emailSecondary A String with the user's secondary email address
     */ 
    public void setEmailSecondary(String emailSecondary) {
        this.emailSecondary = emailSecondary;
    }


    /**
     * @return phoneSecondary A String with the user's secondary phone #
     */ 
    public String getPhoneSecondary() { return this.phoneSecondary; }

    /**
     * @param phoneSecondary A String with the user's secondary phone #
     */ 
    public void setPhoneSecondary(String phoneSecondary) {
        this.phoneSecondary = phoneSecondary;
    }


    /**
     * @return status A String with the user's current system status
     */ 
    public String getStatus() { return this.status; }

    /**
     * @param status A String with the user's current system status
     */ 
    public void setStatus(String status) { this.status = status; }


    /**
     * @return activationKey A String, currently unused
     */ 
    public String getActivationKey() { return this.activationKey; }

    /**
     * @param activationKey A String, currently unused
     */ 
    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }


    /**
     * @return loginTime A Long instance with user's login time
     */ 
    public Long getLoginTime() { return this.loginTime; }

    /**
     * @param loginTime A Long instance with user's login time
     */ 
    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    /**
     * @return cookieHash A String with the user's cookie
     */ 
    public String getCookieHash() {
        return this.cookieHash;
    }

    /**
     * @param cookieHash A String with the user's cookie
     */ 
    public void setCookieHash(String cookieHash) {
        this.cookieHash = cookieHash;
    }


    /**
     * @return institution The Institution of this user
     */ 
    public Institution getInstitution() { return this.institution; }

    /**
     * @param institution Set the Institution for this user
     */ 
    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
