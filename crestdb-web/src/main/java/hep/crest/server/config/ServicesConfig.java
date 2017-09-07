package hep.crest.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
//import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;


@Configuration
@ComponentScan("hep.crest.data")
@EnableAspectJAutoProxy
@PropertySource("classpath:crest.properties")
public class ServicesConfig {

	
	@Bean(name = "jacksonMapper")
	public ObjectMapper getJacksonMapper() {
		ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    mapper.setDateFormat(new ISO8601DateFormat());
	    return mapper;
	}
	
//	@Bean(name = "cachingPolicy")
//	public CachingPolicyService getCachingPolicy(Environment environment) {
//		Integer iovgroupssnapmaxage = environment.getProperty("cdb.iovsgroups.snapshot.maxage", Integer.class, new Integer(60));
//		Integer iovssnapmaxage = environment.getProperty("cdb.iovs.snapshot.maxage", Integer.class, new Integer(60));
//		Integer maxage = environment.getProperty("cdb.iovs.maxage", Integer.class, new Integer(60));
//		CachingPolicyService cachesvc = new CachingPolicyService(iovgroupssnapmaxage, iovssnapmaxage, maxage);
//		return cachesvc;
//	}
}