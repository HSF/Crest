package hep.crest.server.config;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;

import hep.crest.server.filters.CORSFilter;
import hep.crest.server.filters.CacheControlFilter;
import hep.crest.server.swagger.api.AdminApi;
import hep.crest.server.swagger.api.FoldersApi;
import hep.crest.server.swagger.api.FsApi;
import hep.crest.server.swagger.api.GlobaltagmapsApi;
import hep.crest.server.swagger.api.GlobaltagsApi;
import hep.crest.server.swagger.api.IovsApi;
import hep.crest.server.swagger.api.PayloadsApi;
import hep.crest.server.swagger.api.TagsApi;
import io.swagger.jaxrs.config.BeanConfig;

/**
 * @author formica
 *
 */
public class JerseyConfig extends ResourceConfig {

    /**
     * Default ctor.
     */
    public JerseyConfig() {
        register(AdminApi.class);
        register(GlobaltagsApi.class);
        register(TagsApi.class);
        register(GlobaltagmapsApi.class);
        register(IovsApi.class);
        register(PayloadsApi.class);
        register(FsApi.class);
        register(FoldersApi.class);
        register(MultiPartFeature.class);
        register(CacheControlFilter.class);
        register(CORSFilter.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }

    // @PostConstruct
    /**
     * @return
     */
    public void init() {
        // Register components where DI is needed
        this.configureSwagger();
    }

    /**
     * @param clazz
     *            the class
     * @return
     */
    public void jerseyregister(Class<?> clazz) {
        super.register(clazz);
    }

    /**
     * @return
     */
    private void configureSwagger() {
        this.register(io.swagger.jaxrs.listing.ApiListingResource.class);
        this.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        beanConfig.setSchemes(new String[] {"http"});
        beanConfig.setBasePath("/crestapi");
        beanConfig.setResourcePackage("hep.crest.server.swagger.api");
        beanConfig.setScan(true);
    }
}
