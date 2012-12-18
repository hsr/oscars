package net.es.oscars.pss.eompls.junos;


/**
 * creates ESNet SDN-style names
 * @author haniotak
 *
 */
public class SDNNameGenerator {

    public String getFilterName(String gri, String type) {

        String base = oscarsName(gri);
        if (type.equals("stats")) {
            return base+"_stats";
        } else if (type.equals("policing")) {
            return base+"_policing";
        } else {
            return base;
        }
    }

    public String getIswitchTerm(String gri) {
        String base = oscarsName(gri);
        return base;
    }
    

    public String getInterfaceDescription(String gri, Long bandwidth) {
        String base = oscarsName(gri);
        return base + ":"+bandwidth+":oscars-l2circuit:show:circuit-intercloud";
    }

    public String getL2CircuitDescription(String gri) {
        return oscarsName(gri);
    }

    public String getLSPName(String gri) {
        return oscarsName(gri);
    }

    public String getPathName(String gri) {
        return oscarsName(gri);
    }

    public String getPolicerName(String gri) {
        return oscarsName(gri);
    }

    public String getPolicyName(String gri) {
        return oscarsName(gri);
    }

    public String getCommunityName(String gri) {
        return oscarsName(gri);
    }

    public String getVplsName(String gri) {
        return oscarsName(gri);
    }


    public static String oscarsName(String gri) {
        String header = "oscars_";

        String circuitStr = gri;
        
        // gri should look like domain.name.com-1234

        // the maximum length is 32 characters so we need to make sure that the "oscars_" portion fits on
        if ((header + circuitStr).length() > 32) {
            int split_offset = circuitStr.lastIndexOf('-');

            if (split_offset == -1) {
        // it's not of the form domain-####, so remove from the
        // beginning of the string until we have a proper length string
        // so we can prepend the header.
                int offset = header.length() + circuitStr.length() - 32;
                circuitStr = circuitStr.substring(offset, circuitStr.length());
            } else {
                // here we likely have something of the form "domain-#"
                String domainSegment = circuitStr.substring(0,split_offset-1);
                String tailSegment   = circuitStr.substring(split_offset, circuitStr.length());

        // hack off the end of the domain section so that we have a
        // proper length string once we prepend the header.
                domainSegment = domainSegment.substring(0, 32 - header.length() - tailSegment.length());

                circuitStr = domainSegment+tailSegment;
            }
        }

        circuitStr = header + circuitStr;

        // replace dots with _ 
        circuitStr = circuitStr.replaceAll("\\.", "_");
        // don't allow junk characters - safety 
        circuitStr = circuitStr.replaceAll("[^a-zA-Z0-9\\-\\_]+", "");
        
        return circuitStr;
    }
    
    private static SDNNameGenerator instance;
    private SDNNameGenerator() {
    }
    public static SDNNameGenerator getInstance() {
        if (instance == null) {
            instance = new SDNNameGenerator();
        }
        return instance;
    }

    public static Integer getOscarsCommunity(String gri) {
        // return OSCARS
        return 672277;
    }
    public static String numbers(String gri) {

        String circuitStr = gri;
        circuitStr = circuitStr.replaceAll("[^0-9]+", "");

        return circuitStr;
    }

}
