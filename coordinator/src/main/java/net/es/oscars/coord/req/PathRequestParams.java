package net.es.oscars.coord.req;

import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.TeardownPathContent;

public class PathRequestParams {
    public static final String CREATEPATHCONTENT = "CreatePathContent";
    public static final String TEARDOWNPATHCONTENT = "TeardownPathContent";
    private CreatePathContent   createPathContent = null;
    private TeardownPathContent teardownPathContent = null;
    private String type = null;
    
    public PathRequestParams (CreatePathContent createPathContent) {
        this.createPathContent = createPathContent;
        this.type = PathRequestParams.CREATEPATHCONTENT;
    }

    public PathRequestParams (TeardownPathContent teardownPathContent) {
        this.teardownPathContent = teardownPathContent;
        this.type = PathRequestParams.TEARDOWNPATHCONTENT;
    }
    
    public CreatePathContent getCreatePathContent() {
        return this.createPathContent;
    }
 
    public TeardownPathContent getTeardownPathContent() {
        return this.teardownPathContent;
    }
    
    public String getType() {
        return this.type;
    }
}