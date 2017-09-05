package hep.crest.server.swagger.api.factories;

import hep.crest.server.swagger.api.IovsApiService;
import hep.crest.server.swagger.api.impl.IovsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
public class IovsApiServiceFactory {
    private final static IovsApiService service = new IovsApiServiceImpl();

    public static IovsApiService getIovsApi() {
        return service;
    }
}

