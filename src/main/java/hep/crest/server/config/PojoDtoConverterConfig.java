package hep.crest.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the converters.
 * It scans the package where the mappers are located.
 * @author formica
 */
@Configuration
@ComponentScan(basePackages = "hep.crest.server.converters")  // Scan the package where mappers are
// located
public class PojoDtoConverterConfig {

}
