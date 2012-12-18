package net.es.oscars.utils.validator;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class DataValidator {
    
    public static final String VALIDATE_METHOD = "validator";
    public static final String CLASS_APPENDIX = "Validator";
    public static final String PACKAGE_PREFIX = "net.es.oscars.utils.validator.wrappers";
       
    /**
     * Validate the content of the object. Throws a RuntimeException when data is not valid.
     * @param obj to validate
     * @throws RuntimeException
     */
    @SuppressWarnings("unchecked")
    public static void validate (Object obj, boolean canBeNull) throws RuntimeException {
        // Test if the object has the method doValidateObject(). If it does, invoke it,
        // otherwise load the Validator class for that object and invoke its doValidateObject()
        // method.
        
        if (obj == null) {
            // Validating a null object should always fail when canBeNull is true.
            // TODO: log (with stack trace ?)
            if ( ! canBeNull) {
                throw new RuntimeException ("null object cannot be validated.");
            } else {
                return;
            }
        }
        // Retrieve class name from the object
        Class objClass = obj.getClass();
        // If there a validator method in the class
        Method validatorMethod = null;
        try {
            validatorMethod = objClass.getMethod(VALIDATE_METHOD, objClass);
        } catch (NoSuchMethodException e1) {
            // We can ignore this exception since it just means that the object does not have a validator method.
        } catch (SecurityException e2) {
            // We can ignore this exception since it just means that the object does not have a valid validator method.
        }
        if (validatorMethod == null) {
            // Check if there is a validator class that handles this object.
            try {
                // Retrieve the validator wrapper class
                Class validatorClass = Class.forName (PACKAGE_PREFIX + "." + objClass.getName() + CLASS_APPENDIX);
                // Retrieve the validator method
                validatorMethod = validatorClass.getMethod(VALIDATE_METHOD, objClass);
            } catch (ClassNotFoundException e1) {
                // No wrapper. Cannot validate this object
                // TODO: should be loggued
                System.out.println ("VALIDATOR: object of class " + objClass.getName() + " does not have validator method nor a Validator wrapper class " +
                                    PACKAGE_PREFIX + "." + objClass.getName() + CLASS_APPENDIX);
                return;
            } catch (NoSuchMethodException e2) {
                // No validator class
                // TODO: should be loggued
                System.out.println ("VALIDATOR: validator class " + PACKAGE_PREFIX + "." + objClass.getName() + CLASS_APPENDIX + " does not have a static method " +
                                    VALIDATE_METHOD + "(" + objClass.getName() + ")"); 
                return;
            } 
            // invoke the validator method
            try {
                validatorMethod.invoke (null, obj);
                return;
                
            } catch (IllegalAccessException e1) {
                // TODO: should be loggued
                System.out.println ("VALIDATOR: validator class " + PACKAGE_PREFIX + "." + objClass.getName() + CLASS_APPENDIX + " is throwing " + e1);
                return;
            } catch (InvocationTargetException e2) {
                if(e2 != null && e2.getCause() instanceof RuntimeException ){
                    throw (RuntimeException) e2.getCause();
                }else{
                    System.out.println ("VALIDATOR: validator class " + PACKAGE_PREFIX + "." + objClass.getName() + CLASS_APPENDIX + " is throwing " + e2);
                }
            } 
        }
    }
}
