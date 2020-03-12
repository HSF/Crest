package hep.crest.data.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.sqlite.SQLiteDataSource;

/**
 * Configuration for sqlite datasource.
 * @version %I%, %G%
 * @author formica
 *
 */
@Configuration
@EnableJpaRepositories(
        basePackages = { "hep.crest.data.repositories", "hep.crest.data.monitoring.repositories" })
@EnableTransactionManagement
public class SqliteConfig {

    /**
     * Build the sqlite datasource on a local disk. 
     * @return DataSource
     */
    @Bean
    @Profile("sqlite")
    public DataSource dataSource() {
        final DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

        // Set the driver name.
        dataSourceBuilder.driverClassName("org.sqlite.JDBC");
        
        // The file locator should be provided via properties.
        dataSourceBuilder.url("jdbc:sqlite:/tmp/crestsqlite.db");

        dataSourceBuilder.type(org.sqlite.SQLiteDataSource.class);

        final DataSource ds = dataSourceBuilder.build();

        // Set the shared cache property to true.
        ((SQLiteDataSource) ds).setSharedCache(true);
        return ds;
    }

}
