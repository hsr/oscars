package net.es.oscars.tools.utils;

import java.util.*;
import java.io.*;

import net.es.oscars.database.hibernate.*;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.authN.common.AuthNCore;
import net.es.oscars.authN.common.Jcrypt;
import net.es.oscars.authN.beans.*;
import net.es.oscars.authN.dao.*;

import org.apache.log4j.*;
import org.hibernate.*;

/**
 * IDCUserUtil is a command-line client for adding/deleting users
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCUserUtil extends IDCCmdUtil{
    
    private AuthNCore core = null;
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHN);
    private static String context = ConfigDefaults.CTX_SDK;
    
    public IDCUserUtil(){
        core = AuthNCore.getInstance();
        this.dbname = core.getDbname(); // sets value in IDCCmdUtil
    }
    
    /**
     * Main logic that adds user to the database
     *
     */
    public void addUser(){
        Scanner in = new Scanner(System.in);
        String input = null;
        ArrayList<UserAttribute> userAttrs = new ArrayList<UserAttribute>();

        Session session = core.getSession();
        session.beginTransaction();
        User user = new User();
        System.out.println("* indicates a required field");
        /* Get login name */
        user.setLogin(this.readInput(in, "Login", "", true));
        
        /* Get password */
        boolean mismatch = true;
        String pwd1 = null;
        while (mismatch){
            pwd1 =  this.readInput(in, "Password", "", true);
            String pwd2 =  this.readInput(in, "Confirm Password", "", true);
            if (!pwd1.equals(pwd2)) {
                System.err.println("password mismatch");
            } else {
                mismatch = false;
            }
        }
        user.setPassword(Jcrypt.crypt(core.getSalt(),pwd1));
        
        /* Get name and contact info */
        user.setFirstName(this.readInput(in, "\010First Name", "", true));
        user.setLastName(this.readInput(in, "Last Name", "", true));
        input = this.readInput(in, "Cert Subject", "", false);
        if (input != null) {
            try{
                input = this.checkDN(input);
            }catch(Exception e){
                System.err.println(e.getMessage());
                System.exit(0);
            }
        }else { input = ""; }
        user.setCertSubject(input);
        input = this.readInput(in, "Cert Issuer", "", false);
        if (input != null) {
            try{
                input = this.checkDN(input);
            }catch(Exception e){
                System.err.println(e.getMessage());
                System.exit(0);
            }
        }else { input = ""; }
        user.setCertIssuer(input);
        user.setInstitution(this.selectInstitution(in, "user's organization", session));
        userAttrs = this.selectRoles(in);
        user.setDescription(this.readInput(in, "Personal Description", "", false));
        user.setEmailPrimary(this.readInput(in, "Email(Primary)", "", true));
        user.setEmailSecondary(this.readInput(in, "Email(Secondary)", "", false));
        user.setPhonePrimary(this.readInput(in, "Phone(Primary)", "", true));
        user.setPhoneSecondary(this.readInput(in, "Phone(Secondary)", "", false));
        
        /* Save the user and attributes */
        session.save(user);
        for(UserAttribute userAttr : userAttrs){
            userAttr.setUser(user);
            session.save(userAttr);
        }
        session.getTransaction().commit();
        
        System.out.println("New user '" + user.getLogin() + "' added.");
    }
    
    /**
     * Main llogic for deleting a user from the database
     */
    public void removeUser(){
        Scanner in = new Scanner(System.in);
        Session session = core.getSession();
        session.beginTransaction();
        User user = this.selectUser(in);
        System.out.print("Are you sure you want to delete '" + 
                            user.getLogin() + "'? [y/n] ");
        String ans = in.next();
        
        if(ans.toLowerCase().startsWith("y")){
            session.delete(user);
            System.out.println("User '" + user.getLogin() + "' deleted.");
        }else{
            System.out.println("Operation cancelled. No user deleted.");
        }
       
        session.getTransaction().commit();
    }
    
    /**
     * CheckDN  check for the input DN to be in comma separated format starting
     *    with the CN element.
     *    Copied from ServerUtils - mrt
     * @param DN string containing the input DN
     * @return String returning the DN, possibily in reverse order
     */
    private String checkDN(String DN) throws Exception {

        String[] dnElems = null;

        dnElems = DN.split(",");
        if (dnElems.length < 2)  {
            /* TODO look for / separated elements */
            throw new Exception
                    ("Please input cert issuer and subject names as comma separated elements");
         }
        if (dnElems[0].startsWith("CN")) { return DN;}
        /* otherwise reverse the order */
        String dn = " " + dnElems[0];
        for (int i = 1; i < dnElems.length; i++) {
            dn = dnElems[i] + "," + dn;
        }
        dn = dn.substring(1);
        return dn;
    }
    
    /**
     * Prints the current list of users in the database and allows the
     * user to choose one
     *
     * @param in the Scanner to use for accepting input
     * @return the selected Institution
     */
    private User selectUser(Scanner in){
        UserDAO userDAO = new UserDAO(this.dbname);
        List<User> users = userDAO.list();
        int i = 1;
        
        System.out.println();
        for(User user : users){
            System.out.println(i + ". " + user.getLogin());
            i++;
        }
        
        System.out.print("Select the user to delete (by number): ");
        int n = in.nextInt();
        in.nextLine();
        
        if(n <= 0 || n > users.size()){
            System.err.println("Invalid user number '" +n + "' entered");
            System.exit(0);
        }
        
        return users.get(n-1);
    }
    
    /**
     * Prints the current list of attributes in the database and allows the
     * user to choose one or more from the list
     *
     * @param in the Scanner to use for accepting input
     * @return the selected UserAttributes (with userid set to null)
     */
    private ArrayList<UserAttribute> selectRoles(Scanner in){
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        List<Attribute> attrs = attrDAO.list();
        ArrayList<UserAttribute> userAttrs = new ArrayList<UserAttribute>();
        int i = 1;
        
        System.out.println();
        for(Attribute attr : attrs){
            System.out.println(i + ". " + attr.getValue());
            i++;
        }
        
        System.out.print("Select the user's role(s) (numbers separated by spaces): ");
        String line = in.nextLine();
        StringTokenizer st = new StringTokenizer(line, " ");
        while(st.hasMoreTokens()){
            int n = 0;
            try{
                n = Integer.parseInt(st.nextToken());
            }catch(Exception e){
                System.out.println("Non-numeric value entered in role list");
                System.exit(0);
            }
            if(n <= 0 || n > attrs.size()){
                System.err.println("Invalid role number '" + n + "' entered");
                System.exit(0);
            }
            UserAttribute userAttr = new UserAttribute();
            userAttr.setAttribute(attrs.get(n-1));
            userAttrs.add(userAttr);
        }
        
        return userAttrs;
    }
    
    /**
     * Private class that masks password input
     */
    private class EraserThread implements Runnable {
        private boolean stop;
        
        /**
        * Begin masking...display asterisks (*)
        */
        public void run () {
            stop = true;
            while (stop) {
                System.out.print("\010 ");
                try{
                    Thread.currentThread().sleep(1);
                }catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }
        
        /**
        * Instruct the thread to stop masking
        */
        public void stopMasking() {
            this.stop = false;
        }
    }
    
    public static void main(String[] args){

       System.out.println("starting with context "+ context);
       cc.setContext(context);
       cc.setServiceName(ServiceNames.SVC_AUTHN);
       try {
           // System.out.println("loading manifest from ./config/"+ConfigDefaults.MANIFEST);
           cc.loadManifest(ServiceNames.SVC_AUTHN,  ConfigDefaults.MANIFEST); // manifest.yaml
           Logger.getRootLogger().setLevel(Level.OFF);
       } catch (ConfigException ex) {
           System.out.println("caught ConfigurationException " + ex.getMessage());
           System.exit(-1);
       }

        IDCUserUtil util = new IDCUserUtil();
        if(args[0] != null && args[0].equals("remove")){
            util.removeUser();
        }else{
            util.addUser();
        }
    }
}
