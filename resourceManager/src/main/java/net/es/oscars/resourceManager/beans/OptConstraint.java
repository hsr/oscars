package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * OptConstraint is the Hibernate bean for for the rm.optConstraints table.
 */
public class OptConstraint extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

    private int seqNumber;          //  Order of this optConstraint in  optConstraintList
    
    /** persistent field */
    private Integer id;

   /* @S bhr */
    /** persistent field, type of the value (string,for now)*/
    private String constraintType = "String";

    /** persistent field */
    private String keyName;
    
  /*  // persistent field 
    private String category;*/
    
    /** persistent field */
    private String value;
    
    /** persistent field */
    private Integer reservationId;
   
    /* @E bhr */ 
    
    /** default constructor */
    public OptConstraint() { }

    /**
     * @return the current id of this entry
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id the is to set
     */ 
    public void setId(Integer id) {
        this.id = id;
    }
  
    /*@S bhr*/
    
    /**
     * @return seqNumber int with this report's position in list
     */
    public int getseqNumber() {
        return this.seqNumber;
    }

    /**
     * @param num not actually settable
     */
    public void setseqNumber(int num) {
        this.seqNumber = num;
    }
    
    
    /**
     *  @param constraintType 
     */
    
    public void setConstraintType(String constraintType){
        this.constraintType = constraintType;
    }
    /**
     * 
     * @return contents of the constraintType as an XML string
     */
    public String getConstraintType() {
        return this.constraintType;
    }
    
    /**
     *  @param keyName 
     */
    
    public void setKeyName(String keyName){
        this.keyName = keyName;
    }
    
    /**
     * 
     * @return contents of the keyName which specifies the category of the optional constraint
     */
    public String getKeyName() {
        return this.keyName;
    }
    
    /**
     * @return the current id of this entry
     */ 
    public Integer getReservationId() { return this.reservationId; }

    /**
     * @param id the is to set
     */ 
    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }


    /* @E bhr*/
    
    /**
     * @param catatory String that identifies the category of an optional constraint
     */
    /*@S bhr*/
 /*   public void setCategory(String category){
        this.category = category;
    }*/
    /*@E bhr*/
    /**
     * 
     * @return the category
     */
    /*@S bhr*/
/*    public String getCategory() {
        return this.category;
    }*/
    /*@E bhr*/

    	
    /**
     * 
     * @param value contains an xml string that defines the contents of the constraint
     */
    public void setValue(String value){
        this.value = value;
    }
    /**
     * 
     * @return contents of the constraint as an XML string
     */
    public String getValue() {
        return this.value;
    }
    
    
    
    
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nOptional constraint of type " + getKeyName() + "\n");
        if (this.getValue() != null){
            sb.append("value is \n" + getValue() + "\n");
        }

        return sb.toString();
    }
}
