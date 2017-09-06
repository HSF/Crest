package hep.crest.server;
/**
 * 
 */


import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


//@Configuration
//@EnableAutoConfiguration
//@ComponentScan
@SpringBootApplication
@EnableJpaRepositories("hep.crest.data")
@EntityScan("hep.crest.data")
@ComponentScan("hep.crest")
public class Application extends SpringBootServletInitializer {

	@Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

        };
    }
    
    public static void main(String[] args) {
    	new Application()
		.configure(new SpringApplicationBuilder(Application.class))
		.run(args);
    }
}