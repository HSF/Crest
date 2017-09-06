package hep.crest.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan("hep.crest.server")
//@//EnableLoadTimeWeaving
@EnableAspectJAutoProxy
public class AspectJConfig {

}