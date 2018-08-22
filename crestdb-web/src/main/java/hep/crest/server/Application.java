package hep.crest.server;
/**
 * 
 */


import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;

import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.WebResourceCollection;

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
	@Bean
    public WebServerFactoryCustomizer containerCustomizer() {
        return new WebServerFactoryCustomizer() {
			@Override
			public void customize(WebServerFactory factory) {
				if (factory.getClass().isAssignableFrom(UndertowServletWebServerFactory.class)) {
                	UndertowServletWebServerFactory undertowContainer = (UndertowServletWebServerFactory) factory;
                    undertowContainer.addDeploymentInfoCustomizers(new ContextSecurityCustomizer());
                }
			}
        };
    }

    private static class ContextSecurityCustomizer implements UndertowDeploymentInfoCustomizer {

        @Override
        public void customize(io.undertow.servlet.api.DeploymentInfo deploymentInfo) {
            SecurityConstraint constraint = new SecurityConstraint();
            WebResourceCollection traceWebresource = new WebResourceCollection();
            traceWebresource.addUrlPattern("/*");
            traceWebresource.addHttpMethod(HttpMethod.TRACE.toString());
            constraint.addWebResourceCollection(traceWebresource);
            deploymentInfo.addSecurityConstraint(constraint);
        }

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