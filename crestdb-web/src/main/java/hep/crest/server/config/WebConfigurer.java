package hep.crest.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import hep.crest.data.config.CrestProperties;

@Configuration
class WebConfigurer implements WebMvcConfigurer {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CrestProperties cprops;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String extfpath = "file://" + cprops.getWebstaticdir();
		log.info("Adding external path for static web resources => {}",extfpath);
		registry.addResourceHandler("/ext/**").addResourceLocations(extfpath);
	}

}
