package hep.crest.server.config;

import hep.crest.server.repositories.monitoring.IMonitoringRepository;
import hep.crest.server.repositories.monitoring.JdbcMonitoringRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Repository configuration.
 *
 * @author formica
 */
@Configuration
@ComponentScan("hep.crest.server")
@Slf4j
public class WebRepositoryConfig {

    /**
     * Create a monitoring bean.
     *
     * @param mainDataSource the DataSource
     * @param ctn the helper for table names
     * @return IMonitoringRepository
     */
    @Bean
    public IMonitoringRepository iMonitoringRepository(@Qualifier("dataSource") DataSource mainDataSource,
                                                       @Qualifier("crestTableNames") CrestTableNames ctn) {
        final JdbcMonitoringRepository bean = new JdbcMonitoringRepository(mainDataSource);
        // Set default schema and table name taken from properties.
        bean.setCrestTableNames(ctn);
        return bean;
    }
}
