package hep.crest.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

/**
 * @author formica
 */
@SpringBootApplication
@EntityScan(basePackages = "hep.crest.server")
@ComponentScan(basePackages = {"hep.crest.server", "plugin"})
@Slf4j
public class Application {

    /**
     * @param ctx the ApplicationContext
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            log.debug("Let's inspect the beans provided by Spring Boot:");

            final String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (final String beanName : beanNames) {
                log.debug(beanName);
            }
        };
    }

    /**
     * @param args the arguments.
     * @return
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
