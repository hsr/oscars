package net.es.oscars.pss.eompls.alu;



public class ALUNameGenerator {
    private static ALUNameGenerator instance;
    private ALUNameGenerator() {
    }
    public static ALUNameGenerator getInstance() {
        if (instance == null) {
            instance = new ALUNameGenerator();
        }
        return instance;
    }

    public String getLSPName(String gri) {
        return gri+"_lsp";
    }

    public String getPathName(String gri) {
        return gri+"_path";
    }

    public String numbers(String gri) {

        String circuitStr = gri;
        circuitStr = circuitStr.replaceAll("[^0-9]+", "");
        
        return circuitStr;
    }



    /**
     * @deprecated
     * @param gri
     * @return
     */

    public String getEpipeId(String gri) {
        return numbers(gri);
    }

    /**
     * @deprecated
     * @param gri
     * @return
     */

    public String getSdpId(String gri) {
        return numbers(gri);
    }

    /**
     * @deprecated
     * @param gri
     * @return
     */

    public String getQosId(String gri) {
        return numbers(gri);
    }

}
