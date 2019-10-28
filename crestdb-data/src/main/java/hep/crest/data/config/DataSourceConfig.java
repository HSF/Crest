package hep.crest.data.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author formica
 *
 */
@Configuration
@ComponentScan("hep.crest.data.repositories")
public class DataSourceConfig {

    /**
     * @param ds
     *            the DataSource
     * @return JdbcTemplate
     */
    @Bean(name = "jdbcMainTemplate")
    public JdbcTemplate getMainTemplate(@Autowired DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
