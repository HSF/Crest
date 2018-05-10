package hep.crest.server;
/**
 * 
 */


import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
	
/*	@Bean
	public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
	    UndertowEmbeddedServletContainerFactory factory = 
	      new UndertowEmbeddedServletContainerFactory();
	     
	    factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
	        @Override
	        public void customize(io.undertow.Undertow.Builder builder) {
	            builder.addHttpListener(8080, "0.0.0.0");
	        }
	    });
	     
	    return factory;
	}
*/    
    public static void main(String[] args) {
    	new Application()
		.configure(new SpringApplicationBuilder(Application.class))
		.run(args);
    }
}