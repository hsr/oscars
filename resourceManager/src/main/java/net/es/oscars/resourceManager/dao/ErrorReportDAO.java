package net.es.oscars.resourceManager.dao;

import java.util.List;
import java.util.ArrayList;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.ErrorReportData;
import org.apache.log4j.Logger;

/**
 * ErrorReportDAO is the data access object for the rm.errorReports table.
 *
 * @author Mary Thompson (mrthompson@lbl.gov)
 */
public class ErrorReportDAO extends GenericHibernateDAO<ErrorReportData, Integer> {
    private Logger log;
    private List<ErrorReportData> errorData;

    public ErrorReportDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.errorData =  new ArrayList<ErrorReportData>();
    }
    /*
    public List<ErrorReportData> listByTransId(String transId){
        String hsql = "from ErrorReport r where r.transId = :transId";
        this.errorData = this.getSession().createQuery(hsql)
                                        .setString("transId", transId)
                                        .list();
        return this.errorData;
    }
    */

    public List<ErrorReportData> listByTransId(String transId){
         String sql = "select * from errorReports r " +
                     "where  r.transId = ?";
         List<ErrorReportData> ers =
               (List<ErrorReportData>) this.getSession().createSQLQuery(sql)
                                             .addEntity(ErrorReportData.class)
                                             .setString(0, transId)
                                             .list();
         return ers;
     }
}
