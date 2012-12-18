package net.es.oscars.nsibridge.task;



import net.es.oscars.nsibridge.beans.QueryRequest;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionServiceRequester;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryConfirmedType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class QueryTask extends Task  {
    private QueryRequest request;
    private static final Logger log = Logger.getLogger(QueryTask.class);
    public QueryTask(QueryRequest request) {
        this.scope = "nsi";
        this.request = request;
    }


    public void onRun() throws TaskException {
        super.onRun();
        try {
            String operation = request.getOperation().value();
            List<String> connIds = request.getQueryFilter().getConnectionId();
            List<String> gris = request.getQueryFilter().getGlobalReservationId();


            log.debug("operation: "+operation);
            for (String connId : connIds) {
                log.debug("connId "+connId);
            }

            for (String gri : gris) {
                log.debug("gri "+gri);
            }


            CommonHeaderType reqHd = request.getInHeader();

            String reqReplyTo =reqHd.getReplyTo();
            CommonHeaderType hd = NSI_Util.makeNsiOutgoingHeader(reqHd);

            log.debug("replyTo: "+reqReplyTo);
            URL url;
            try {
                url = new URL(reqReplyTo);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }

            ConnectionServiceRequester client = new ConnectionServiceRequester();
            ConnectionRequesterPort port = client.getConnectionServiceRequesterPort();
            BindingProvider bp = (BindingProvider) port;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());

            Holder outHolder = new Holder<CommonHeaderType>();
            outHolder.value = hd;
            QueryConfirmedType qct = new QueryConfirmedType();
            port.queryConfirmed(qct, outHolder);


        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }


        System.out.println(this.id+" ran!");
        this.onSuccess();
    }

}
