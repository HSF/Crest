package hep.crest.server.config;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Component;

import hep.crest.server.filters.AuthenticationFilter;
import hep.crest.server.filters.AuthorizationFilter;
import hep.crest.server.filters.CacheControlFilter;
import hep.crest.server.swagger.api.AdminApi;
import hep.crest.server.swagger.api.FsApi;
import hep.crest.server.swagger.api.GlobaltagmapsApi;
import hep.crest.server.swagger.api.GlobaltagsApi;
import hep.crest.server.swagger.api.IovsApi;
import hep.crest.server.swagger.api.MonitoringApi;
import hep.crest.server.swagger.api.PayloadsApi;
import hep.crest.server.swagger.api.RuninfoApi;
import hep.crest.server.swagger.api.TagsApi;
import io.swagger.jaxrs.config.BeanConfig;



//@Component
//@ApplicationPath("/crestapi")
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {          
		//packages("hep.crest.server");
		register(AdminApi.class);
		register(GlobaltagsApi.class);
		register(TagsApi.class);
		register(GlobaltagmapsApi.class);
		register(IovsApi.class);
		register(PayloadsApi.class);
		register(FsApi.class);
		register(MultiPartFeature.class);
		////register(AuthenticationFilter.class);
		register(CacheControlFilter.class);
		property(ServletProperties.FILTER_FORWARD_ON_404, true);
	}
	
//	@PostConstruct
	public void init() {
		 // Register components where DI is needed
		this.configureSwagger();
	}

	public void jerseyregister(Class<?> clazz) {
		super.register(clazz);
		return;
	}
	
	private void configureSwagger() {
		this.register(io.swagger.jaxrs.listing.ApiListingResource.class);
		this.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("1.0");
		beanConfig.setSchemes(new String[]{"http"});
		//beanConfig.setHost("localhost:8090");
		beanConfig.setBasePath("/crestapi");
		beanConfig.setResourcePackage("hep.crest.server.swagger.api");
		beanConfig.setScan(true);
	}
}