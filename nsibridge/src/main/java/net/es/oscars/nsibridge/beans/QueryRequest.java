package net.es.oscars.nsibridge.beans;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryType;

public class QueryRequest extends GenericRequest {

    private QueryType query;

    public QueryType getQuery() {
        return query;
    }

    public void setQuery(QueryType query) {
        this.query = query;
    }
}
