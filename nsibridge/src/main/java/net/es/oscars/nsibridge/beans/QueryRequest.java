package net.es.oscars.nsibridge.beans;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryFilterType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryOperationType;

public class QueryRequest extends GenericRequest {

    private QueryOperationType operation;
    private QueryFilterType queryFilter;

    public QueryOperationType getOperation() {
        return operation;
    }

    public void setOperation(QueryOperationType operation) {
        this.operation = operation;
    }

    public QueryFilterType getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(QueryFilterType queryFilter) {
        this.queryFilter = queryFilter;
    }
}
