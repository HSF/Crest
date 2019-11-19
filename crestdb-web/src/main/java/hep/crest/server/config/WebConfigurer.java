package hep.crest.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import hep.crest.data.config.CrestProperties;

/**
 * Web configuration.
 *
 * @author formica
 *
 */
@Configuration
class WebConfigurer implements WebMvcConfigurer {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;

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
