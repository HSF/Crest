package hep.crest.server.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hep.crest.server.serializers.CustomTimeDeserializer;
import hep.crest.server.serializers.CustomTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.concurrent.Executor;

/**
 * Services configuration.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
@Configuration
@ComponentScan("hep.crest.server")
@EnableAspectJAutoProxy
@EnableAsync
public class ServicesConfig {

    /**
     * Create a helper bean.
     * @param cprops the properties.
     * @return CrestTableNames
     */
    @Bean(name = "jacksonMapper")
    public ObjectMapper getJacksonMapper(@Autowired CrestProperties cprops) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
                .appendPattern("xxxx")
                .toFormatter();

        // Create and configure the ObjectMapper
        ObjectMapper mapper = JsonMapper.builder().build();
        mapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(100_000_000).build());

        mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat());
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(OffsetDateTime.class, new CustomTimeSerializer(formatter));
        module.addDeserializer(OffsetDateTime.class, new CustomTimeDeserializer(formatter));
        mapper.registerModule(module);

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

    /**
     * Configure the Executor.
     * @return Executor
     */
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // The core number of threads
        executor.setMaxPoolSize(50);   // The maximum number of threads
        executor.setQueueCapacity(1000); // The queue size before new threads are created
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

}
