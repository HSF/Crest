/**
 * 
 */

package hep.crest.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * @author formica
 *
 */
@SpringBootApplication
public class ApplicationData {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ApplicationData.class);

    /**
     * A runner to dump information from the server.
     *
     * @param ctx
     *            the ApplicationContext
     * @return CommandLineRunner
     */
////
////@///Bean
    public CommandLineRunner cliRunner(ApplicationContext ctx) {
        return args -> {

            log.debug("Let's inspect the beans provided by Spring Boot:");
            final Environment env = ctx.getEnvironment();
            final String server = InetAddress.getLocalHost().getHostName();
            log.debug("local server {}", server);
            log.debug("local server port {} ", env.getProperty("local.server.port"));
        };
    }

    /**
     * @param args
     *            the arguments
     * @return
     */
    public static void main(String[] args) {
        log.info("Start Application Data.....");
        SpringApplication.run(ApplicationData.class);
    }

}
