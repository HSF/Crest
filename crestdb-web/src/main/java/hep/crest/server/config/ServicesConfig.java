package hep.crest.server.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import hep.crest.data.config.CrestProperties;
import hep.crest.server.filters.AuthorizationFilter;
import hep.crest.server.swagger.api.FoldersApi;
import hep.crest.server.swagger.api.RuninfoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Services configuration.
 *
 * @version %I%, %G%
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
     * Activate configuration only on some profiles. This will add API classes like
     * RunInfo and Monitoring.
     *
     * @return JerseyConfig
     */
    @Profile({ "ssl", "cmsprep", "oracle", "test" })
    @Bean(name = "jerseyConfig")
    public JerseyConfig getJerseyResource() {
        final JerseyConfig jc = new JerseyConfig();
        // Register APIs for monitoring.
        jc.jerseyregister(RuninfoApi.class);
        jc.jerseyregister(FoldersApi.class);
        if (!"none".equals(cprops.getSecurity())) {
            // Register authorization filter.
            jc.jerseyregister(AuthorizationFilter.class);
        }
        jc.init();
        return jc;
    }

    /**
     * Activate configuration for test or local profiles. Used also for SVOM.
     *
     * @return JerseyConfig
     */
    @Profile({ "default", "h2", "sqlite", "postgres", "mysql", "pgsvom" })
    @Bean(name = "jerseyConfig")
    public JerseyConfig getJerseyDefaultResource() {
        final JerseyConfig jc = new JerseyConfig();
        if (!"none".equals(cprops.getSecurity())) {
            // Register authorization filter.
            jc.jerseyregister(AuthorizationFilter.class);
        }
        jc.init();
        return jc;
    }

    /**
     * The jackson mapper.
     *
     * @return ObjectMapper
     */
    @Bean(name = "jacksonMapper")
    public ObjectMapper getJacksonMapper() {
        final ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        // Disable the serialization features for DATEs.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
//                // date/time
//                .appendPattern("yyyy-MM-dd HH:mm:ss")
//                // optional fraction of seconds (from 0 to 9 digits)
//                .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
//                // offset
//                .appendPattern("xxx")
//                // create formatter
//                .toFormatter();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        mapper.setDateFormat(new StdDateFormat());
//        JavaTimeModule module = new JavaTimeModule();
//        module.addSerializer(Date.class, new DateSerializer());
//        mapper.registerModule(module);

        return mapper;
    }
    

    /**
     * @return LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }

}
