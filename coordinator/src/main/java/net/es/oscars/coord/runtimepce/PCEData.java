package net.es.oscars.coord.runtimepce;

import java.util.List;

import net.es.oscars.api.soap.gen.v06.OptionalConstraintType;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

public class PCEData  {

    private UserRequestConstraintType    userRequestConstraint = null;
    private ReservedConstraintType       reservedConstraint    = null;
    private CtrlPlaneTopologyContent     topology              = null;
    private List<OptionalConstraintType> optionalConstraint    = null;
    
    public PCEData (UserRequestConstraintType userRequestConstraint,
                    ReservedConstraintType    reservedConstraint,
                    List<OptionalConstraintType> optionalConstraint,
                    CtrlPlaneTopologyContent topologyConstraint) {
        
        this.userRequestConstraint = userRequestConstraint;
        this.reservedConstraint = reservedConstraint;
        this.optionalConstraint = optionalConstraint;
        this.topology = topologyConstraint;
    }
    
    public UserRequestConstraintType getUserRequestConstraint () {
        return this.userRequestConstraint;
    }
    
    public ReservedConstraintType getReservedConstraint () {
        return this.reservedConstraint;
    }

    public List<OptionalConstraintType> getOptionalConstraint() {
        return this.optionalConstraint;
    }
 
    public CtrlPlaneTopologyContent getTopology () {
        return this.topology;
    }
    
    public void setUserRequestConstraint (UserRequestConstraintType userRequestConstraint) {
        this.userRequestConstraint = userRequestConstraint;
    }
    
    public void setReservedConstraint (ReservedConstraintType reservedConstraint) {
        this.reservedConstraint = reservedConstraint;
    }
    
    public void setTopologyConstraint (CtrlPlaneTopologyContent topology) {
        this.topology = topology;
    }
    
    public void setOptionalConstraintType (List<OptionalConstraintType> optionalConstraint) {
        this.optionalConstraint = optionalConstraint;
    }
}

