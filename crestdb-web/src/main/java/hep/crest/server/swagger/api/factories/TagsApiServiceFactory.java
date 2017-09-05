package hep.crest.server.swagger.api.factories;

import hep.crest.server.swagger.api.TagsApiService;
import hep.crest.server.swagger.api.impl.TagsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
public class TagsApiServiceFactory {
    private final static TagsApiService service = new TagsApiServiceImpl();

    public static TagsApiService getTagsApi() {
        return service;
    }
}

