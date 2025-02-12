package hep.crest.server.config;

import hep.crest.server.config.filters.CORSFilter;
import hep.crest.server.config.filters.CacheControlFilter;
import hep.crest.server.swagger.api.AdminApi;
import hep.crest.server.swagger.api.FoldersApi;
import hep.crest.server.swagger.api.GlobaltagmapsApi;
import hep.crest.server.swagger.api.GlobaltagsApi;
import hep.crest.server.swagger.api.IovsApi;
import hep.crest.server.swagger.api.MonitoringApi;
import hep.crest.server.swagger.api.PayloadsApi;
import hep.crest.server.swagger.api.RuninfoApi;
import hep.crest.server.swagger.api.TagsApi;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Component;

/**
 * Jersey configuration.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
@Component
@Slf4j
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
        super.register(FoldersApi.class);
        super.register(GlobaltagmapsApi.class);
        super.register(IovsApi.class);
        super.register(PayloadsApi.class);
        super.register(RuninfoApi.class);
        super.register(MonitoringApi.class);
        super.register(FoldersApi.class);
        super.register(MultiPartFeature.class);
        // Filters.
        super.register(CacheControlFilter.class);
        super.register(CORSFilter.class);
        // Exception handler
        super.register(JerseyExceptionHandler.class);

        super.property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }

    /**
     * @return
     */
    @PostConstruct
    public void init() {
        log.info("JerseyConfig init");
    }

    /**
     * @param clazz
     *            the class
     * @return
     */
    public void jerseyregister(Class<?> clazz) {
        super.register(clazz);
    }

}
