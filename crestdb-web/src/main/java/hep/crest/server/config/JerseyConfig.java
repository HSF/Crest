package hep.crest.server.config;

import hep.crest.server.swagger.api.AdminApi;
import hep.crest.server.swagger.api.GlobaltagsApi;
import hep.crest.server.swagger.api.GlobaltagmapsApi;
import hep.crest.server.swagger.api.TagsApi;
import hep.crest.server.swagger.api.IovsApi;
import hep.crest.server.swagger.api.PayloadsApi;
import hep.crest.server.swagger.api.MonitoringApi;
import hep.crest.server.swagger.api.RuninfoApi;
import hep.crest.server.swagger.api.FsApi;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;

import hep.crest.server.filters.CORSFilter;
import hep.crest.server.filters.CacheControlFilter;
import io.swagger.jaxrs.config.BeanConfig;

/**
 * Jersey configuration.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
public class JerseyConfig extends ResourceConfig {

    /**
     * Default ctor.
     */
    public JerseyConfig() {
        super();
        // Register all API.
        super.register(AdminApi.class);
        super.register(GlobaltagsApi.class);
        super.register(TagsApi.class);
        super.register(GlobaltagmapsApi.class);
        super.register(IovsApi.class);
        super.register(PayloadsApi.class);
        super.register(FsApi.class);
        super.register(RuninfoApi.class);
        super.register(MonitoringApi.class);
        super.register(MultiPartFeature.class);
        super.register(CacheControlFilter.class);
        super.register(CORSFilter.class);
        super.property(ServletProperties.FILTER_FORWARD_ON_404, true);
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
     * Swagger configuration.
     * @return
     */
    private void configureSwagger() {
        // Register swagger listing classes for jaxrs.
        super.register(io.swagger.jaxrs.listing.ApiListingResource.class);
        super.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        beanConfig.setSchemes(new String[] {"http"});
        // Define the baseapi path: it is used only in swagger ui.
        beanConfig.setBasePath("/crestapi");
        beanConfig.setResourcePackage("hep.crest.server.swagger.api");
        beanConfig.setScan(true);
    }
}
