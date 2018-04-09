package hep.crest.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
//import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import hep.crest.data.config.CrestProperties;
import hep.crest.server.filters.AuthorizationFilter;
import hep.crest.server.swagger.api.MonitoringApi;
import hep.crest.server.swagger.api.RuninfoApi;


@Configuration
@ComponentScan("hep.crest.data")
@EnableAspectJAutoProxy
@EnableAsync
////@PropertySource("classpath:crest.properties")
public class ServicesConfig {

	@Autowired
	private CrestProperties cprops;
	
    @Profile({"prod","wildfly"})
	@Bean(name = "jerseyConfig")
	public JerseyConfig getJerseyResource() {
		JerseyConfig jc = new JerseyConfig();
		jc.jerseyregister(RuninfoApi.class);
		jc.jerseyregister(MonitoringApi.class);
		if (!cprops.getSecurity().equals("none")) {
			jc.jerseyregister(AuthorizationFilter.class);
		}
		jc.init();
	    return jc;
	}
    
    @Profile({"default","dev","h2","sqlite","postgres","mysql"})
	@Bean(name = "jerseyConfig")
	public JerseyConfig getJerseyDefaultResource() {
		JerseyConfig jc = new JerseyConfig();
		if (!cprops.getSecurity().equals("none")) {
			jc.jerseyregister(AuthorizationFilter.class);
		}
		jc.init();
	    return jc;
	}
		
	@Bean(name = "jacksonMapper")
	public ObjectMapper getJacksonMapper() {
		ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    mapper.setDateFormat(new ISO8601DateFormat());
	    return mapper;
	}
	
}