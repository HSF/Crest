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
 * @author formica
 *
 */
@Configuration
@EnableJpaRepositories(
        basePackages = { "hep.crest.data.repositories", "hep.crest.data.monitoring.repositories" })
@EnableTransactionManagement
public class SqliteConfig {

    /**
     * @return DataSource
     */
    @Bean
    @Profile("sqlite")
    public DataSource dataSource() {
        final DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.sqlite.JDBC");
        dataSourceBuilder.url("jdbc:sqlite:/tmp/crestsqlite.db");
        dataSourceBuilder.type(org.sqlite.SQLiteDataSource.class);
        final DataSource ds = dataSourceBuilder.build();
        ((SQLiteDataSource) ds).setSharedCache(true);
        return ds;
    }

}
