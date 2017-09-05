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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


//@Configuration
//@EnableAutoConfiguration
//@ComponentScan
@SpringBootApplication
@EnableJpaRepositories("hep.crest.data")
@EntityScan("hep.crest.data")
public class Application extends SpringBootServletInitializer {

//	@Bean
//	public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
//		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
//		registration.addUrlMappings("/api/*");
//		return registration;
//	}

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
	
//	@Bean
//	public ServletRegistrationBean jerseyServlet() {
//	    ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(), "/rest/*");
//	    // our rest resources will be available in the path /rest/*
//	    registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, JerseyConfig.class.getName());
//	    return registration;
//	}
	
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		BeanConfig beanConfig = new BeanConfig();
//		beanConfig.setVersion("1.0.2");
//		beanConfig.setSchemes(new String[]{"http"});
//		beanConfig.setHost("localhost:8090");
//		beanConfig.setBasePath("/api");
//		beanConfig.setResourcePackage("fr.svom.vhf.server.swagger.api");
//		beanConfig.setScan(true);

       return application.sources(Application.class);
    }
    
    public static void main(String[] args) {
    	new Application()
		.configure(new SpringApplicationBuilder(Application.class))
		.run(args);
    }


	//https://github.com/swagger-api/swagger-core/wiki/Swagger-Core-Jersey-2.X-Project-Setup-1.5#using-the-application-class
	// http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-jersey
	// http://stackoverflow.com/questions/20915528/how-can-i-register-a-secondary-servlet-with-spring-boot
}