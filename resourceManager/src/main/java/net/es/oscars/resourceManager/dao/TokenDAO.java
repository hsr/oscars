package net.es.oscars.resourceManager.dao;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.Token;

/**
 * TokenDAO is the data access object for the rm.tokens table.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class TokenDAO extends GenericHibernateDAO<Token, Integer> {

    public TokenDAO(String dbname) {
        this.setDatabase(dbname);
    }
    
    /**
     * Returns a Token given the token value
     * @param value the value of the token to retrieve
     * @return a Token instance with specified value
     */
    public Token fromValue(String value){
        String hsql = "from Token where value = ?";
        
         return (Token) this.getSession().createQuery(hsql)
                                         .setString(0, value)
                                         .setMaxResults(1)
                                         .uniqueResult();
    }
}
