package net.es.oscars.utils.validator.wrappers.net.es.oscars.api.soap.gen.v06;

import net.es.oscars.utils.validator.DataValidator;

import net.es.oscars.api.soap.gen.v06.ResCreateContent;

public class ResCreateContentValidator {
    /**
     * Validate the content of a ResCreateContent object. Note that object will never be null.
     * @param obj to validate
     * @throws RuntimeException
     */
    public static void validator (ResCreateContent obj) throws RuntimeException {
        DataValidator.validate (obj.getUserRequestConstraint(), false);
    }
}
