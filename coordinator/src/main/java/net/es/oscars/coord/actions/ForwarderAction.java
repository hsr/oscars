package net.es.oscars.coord.actions;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;

@SuppressWarnings("unchecked")
public class ForwarderAction <P,Q> extends CoordAction <P,Q> {

    private static final long       serialVersionUID = 1L;
    private String destDomainId = null;

    public ForwarderAction (String name,
                            CoordRequest request,
                            String destDomainId,
                            P data) {
        super (name, request, null);
        this.setRequestData(data);
        this.destDomainId = destDomainId;
    }
    
    public void execute()  {
        this.setResultData(null);           
        this.executed();
    } 
    
    public String getDestDomainId() {
        return this.destDomainId;
    }
    
}
