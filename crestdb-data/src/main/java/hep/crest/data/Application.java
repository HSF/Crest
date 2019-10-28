/**
 * 
 */

package hep.crest.data;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author formica
 *
 */
@SpringBootApplication
public final class Application {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * A runner to dump information from the server.
     *
     * @param ctx
     *            the ApplicationContext
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            log.debug("Let's inspect the beans provided by Spring Boot:");
            final Environment env = ctx.getEnvironment();
            final String server = InetAddress.getLocalHost().getHostName();
            log.debug("local server {}", server);
            log.debug("local server port {} ", env.getProperty("local.server.port"));
        };
    }
    
    /**
     * Default hidden ctor.
     */
    private Application() {
    }

    /**
     * @param args
     *            the arguments
     * @return
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

}
