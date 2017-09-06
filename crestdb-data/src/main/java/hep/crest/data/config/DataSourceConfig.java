package hep.crest.data.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan("hep.crest.data.repositories")
public class DataSourceConfig {
 
 
	@Primary
	@ConfigurationProperties(prefix="spring.datasource")
    @Bean(name = "daoDataSource")
    public DataSource createMainDataSource() {
	    return DataSourceBuilder.create().build();
    }
	
    
}