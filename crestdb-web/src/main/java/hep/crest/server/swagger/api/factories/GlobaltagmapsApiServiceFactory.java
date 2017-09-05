package hep.crest.server.swagger.api.factories;

import hep.crest.server.swagger.api.GlobaltagmapsApiService;
import hep.crest.server.swagger.api.impl.GlobaltagmapsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
public class GlobaltagmapsApiServiceFactory {
    private final static GlobaltagmapsApiService service = new GlobaltagmapsApiServiceImpl();

    public static GlobaltagmapsApiService getGlobaltagmapsApi() {
        return service;
    }
}

