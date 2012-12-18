package net.es.oscars.tools.utils;

import java.util.*;
import java.io.*;

import org.hibernate.Session;

import net.es.oscars.authN.beans.Institution;
import net.es.oscars.authN.dao.InstitutionDAO;
/*import net.es.oscars.database.*;

import org.apache.log4j.*;
import org.hibernate.*;
*/

/**
 * IDCCmdUtil an abstract class for creating command-line utilities
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCCmdUtil{
    protected String dbname;
    //protected String aaaDbName;
    //protected Logger log;
    
    /**
     * Method to read in user input strings
     *
     * @param in a Scanner used toaccept input
     * @param label a String describing the requested input to the user
     * @param defaultVal the default value to assign if no input provided
     * @param req boolean indicating whether this field is required
     * @return the String input by the user
     */
    protected String readInput(Scanner in, String label, String defaultVal, boolean req){
        System.out.print(label + (req?"*":""));// + " [" + defaultVal + "]: ");
        System.out.print(": ");
        String input = in.nextLine().trim();
        
        if(input.equals("") && (!defaultVal.equals(""))){
            input = defaultVal;
        }else if(input.equals("") && defaultVal.equals("") && req){
            System.err.println("The field '" + label + "' is required.");
            System.exit(0);
        }else if(input.equals("")){
            return null;
        }
        
        return input;
    }
    
    
    /**
     * Prints the current list of institutions in the database and allows the
     * user to choose one
     *
     * @param in the Scanner to use for accepting input
     * @return the selected Institution
     */
    protected Institution selectInstitution(Scanner in, String label, Session session){
        InstitutionDAO instDAO = new InstitutionDAO(this.dbname);
        List<Institution> institutions = instDAO.list();
        int i = 1;
        
        System.out.println();
        System.out.println(i + ". Add new organization...");
        i++;
        for(Institution inst : institutions){
            System.out.println(i + ". " + inst.getName());
            i++;
        }
        
        System.out.print("Select the " + label + " (by number): ");
        int n = in.nextInt();
        in.nextLine();
        
        if(n <= 0 || n > institutions.size() + 1){
            System.err.println("Invalid organization number '" +n + "' entered");
            System.exit(0);
        }
        if(n != 1){
            return institutions.get(n-2);
        }
        
        //add new institution
        Institution inst = new Institution();
        System.out.print("Enter the new organization name: ");
        String newInstName = in.nextLine();
        inst.setName(newInstName.trim());
        session.save(inst);
        
        return inst;
    }
}
