package hep.crest.server.swagger.api.factories;

import hep.crest.server.swagger.api.PayloadsApiService;
import hep.crest.server.swagger.api.impl.PayloadsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
public class PayloadsApiServiceFactory {
    private final static PayloadsApiService service = new PayloadsApiServiceImpl();

    public static PayloadsApiService getPayloadsApi() {
        return service;
    }
}

