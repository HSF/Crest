package hep.crest.server.swagger.api.factories;

import hep.crest.server.swagger.api.GlobaltagsApiService;
import hep.crest.server.swagger.api.impl.GlobaltagsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
public class GlobaltagsApiServiceFactory {
    private final static GlobaltagsApiService service = new GlobaltagsApiServiceImpl();

    public static GlobaltagsApiService getGlobaltagsApi() {
        return service;
    }
}

