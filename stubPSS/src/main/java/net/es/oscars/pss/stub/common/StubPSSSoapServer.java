package net.es.oscars.pss.stub.common;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.pss.soap.gen.PSSService;
import net.es.oscars.pss.soap.gen.PSSPortType;

@OSCARSNetLoggerize( moduleName = ModuleName.PSS)
@OSCARSService (
        implementor = "net.es.oscars.pss.stub.common.StubPSSSoapHandler",
        serviceName = ServiceNames.SVC_PSS,
        config = ConfigDefaults.CONFIG
)
public class StubPSSSoapServer extends OSCARSSoapService<PSSService, PSSPortType> {
    private static StubPSSSoapServer instance;

    public static StubPSSSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new StubPSSSoapServer();
        }
        return instance;
    }

    private StubPSSSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_PSS);
    }
}
