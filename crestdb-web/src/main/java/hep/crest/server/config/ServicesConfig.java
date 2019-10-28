package hep.crest.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import hep.crest.data.config.CrestProperties;
import hep.crest.server.filters.AuthorizationFilter;
import hep.crest.server.swagger.api.MonitoringApi;
import hep.crest.server.swagger.api.RuninfoApi;

/**
 * @author formica
 *
 */
@Configuration
@ComponentScan("hep.crest.data")
@EnableAspectJAutoProxy
@EnableAsync
public class ServicesConfig {

    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;

    /**
     * @return JerseyConfig
     */
    @Profile({ "prod", "wildfly", "ssl", "cmsprep", "oracle" })
    @Bean(name = "jerseyConfig")
    public JerseyConfig getJerseyResource() {
        final JerseyConfig jc = new JerseyConfig();
        jc.jerseyregister(RuninfoApi.class);
        jc.jerseyregister(MonitoringApi.class);
        if (!cprops.getSecurity().equals("none")) {
            jc.jerseyregister(AuthorizationFilter.class);
        }
        jc.init();
        return jc;
    }

    /**
     * @return JerseyConfig
     */
    @Profile({ "test", "default", "dev", "h2", "sqlite", "postgres", "mysql", "pgsvom" })
    @Bean(name = "jerseyConfig")
    public JerseyConfig getJerseyDefaultResource() {
        final JerseyConfig jc = new JerseyConfig();
        if (!cprops.getSecurity().equals("none")) {
            jc.jerseyregister(AuthorizationFilter.class);
        }
        jc.init();
        return jc;
    }

    /**
     * @return ObjectMapper
     */
    @Bean(name = "jacksonMapper")
    public ObjectMapper getJacksonMapper() {
        final ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }
}
