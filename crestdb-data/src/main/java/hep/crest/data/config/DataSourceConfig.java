package hep.crest.data.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
@ComponentScan("hep.crest.data.repositories")
public class DataSourceConfig {
 
	@Autowired
	private CrestProperties cprops;

	@Autowired
	private DataSource ds;

	@Bean(name = "jdbcMainTemplate")
	  public JdbcTemplate getMainTemplate() {
			return new JdbcTemplate(ds);
	}

//	@Primary
//	@ConfigurationProperties(prefix="spring.datasource")
//    @Bean(name = "daoDataSource")
//    public DataSource createMainDataSource() {
//	    return DataSourceBuilder.create().build();
//    }
	
    
}