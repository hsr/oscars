package net.es.oscars.utils.validator.wrappers.net.es.oscars.api.soap.gen.v06;

import net.es.oscars.utils.validator.DataValidator;

import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;

public class UserRequestConstraintTypeValidator {
    /**
     * Validate the content of a UserRequestConstraintType object. Note that object will never be null.
     * 
     * Only validates the UserRequestConstraint.
     * 
     * @param obj to validate
     * @throws RuntimeException
     */
    public static void validator (UserRequestConstraintType obj) throws RuntimeException {
        
        if (obj.getStartTime() >= obj.getEndTime()) {
            // endtime must be larger than endtime
            // TODO: need to log the error
            throw new RuntimeException ("endtime is before starttime");
        }

        // UserRequestConstraint can not be null
        DataValidator.validate (obj.getPathInfo(), false);
    }
}
