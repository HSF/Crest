package hep.crest.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import hep.crest.data.config.CrestProperties;

@Configuration
class WebConfigurer extends WebMvcConfigurerAdapter {

	@Autowired
	private CrestProperties cprops;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String extfpath = "file://" + cprops.getWebstaticdir();
		System.out.println("Adding external path for static web resources..."+extfpath);
		registry.addResourceHandler("/ext/**").addResourceLocations(extfpath);
	}

}