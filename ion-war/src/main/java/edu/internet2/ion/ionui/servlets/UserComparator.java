package edu.internet2.ion.ionui.servlets;

import java.util.Comparator;
//import net.es.oscars.authN.beans.User;
import net.es.oscars.authN.soap.gen.policy.UserDetails;

public class UserComparator<T> implements Comparator<T>{
    private String field;
    private int ascVal;
    
    public UserComparator(String field, boolean asc){
        this.field = field;
        this.ascVal = (asc ? 1 : -1);
    }
    
    public int compare(Object o1, Object o2) {
        //User u1 = (User) o1;
        //User u2 = (User) o2;
        UserDetails u1 = (UserDetails) o1;
        UserDetails u2 = (UserDetails) o2;
        String val1 = "";
        String val2 = "";
        
        if("login".equals(field)){
            val1 = u1.getLogin();
            val2 = u2.getLogin();
        }else if("firstName".equals(field)){
            val1 = u1.getFirstName();
            val2 = u2.getFirstName();
        }else if("lastName".equals(field)){
            val1 = u1.getLastName();
            val2 = u2.getLastName();
        }else if("organization".equals(field)){
            //val1 = u1.getInstitution().getName();
            //val2 = u2.getInstitution().getName();
            val1 = u1.getInstitution();
            val2 = u2.getInstitution();
        }else if("phone".equals(field)){
            val1 = u1.getPhonePrimary();
            val2 = u2.getPhonePrimary();
        }else{
            val1 = u1.getEmailPrimary();
            val2 = u2.getEmailPrimary();
        }
        
        return this.ascVal * val1.toLowerCase().compareTo(val2.toLowerCase());
    }

}
