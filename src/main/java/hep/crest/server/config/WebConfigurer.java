package hep.crest.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
@Configuration
@Slf4j
class WebConfigurer implements WebMvcConfigurer {
    /**
     * Properties.
     */
    private CrestProperties cprops;

    /**
     * Ctor for injection.
     * @param cprops
     */
    @Autowired
    WebConfigurer(CrestProperties cprops) {
        this.cprops = cprops;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#
     * addResourceHandlers(org.springframework.web.servlet.config.annotation.
     * ResourceHandlerRegistry)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        final String extfpath = "file://" + cprops.getWebstaticdir();
        log.info("Adding external path for static web resources => {}", extfpath);
        registry.addResourceHandler("/ext/**").addResourceLocations(extfpath);
    }

}
