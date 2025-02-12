package hep.crest.server.config;

import hep.crest.server.data.repositories.IovGroupsCustom;
import hep.crest.server.data.repositories.IovGroupsImpl;
import hep.crest.server.repositories.triggerdb.ITriggerDb;
import hep.crest.server.repositories.triggerdb.TriggerDb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository configuration.
 *
 * @author formica
 */
@Configuration
@ComponentScan("hep.crest.server")
@EnableJpaRepositories(
        basePackages = "hep.crest.server", // Adjust the base package to your project structure
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
@Slf4j
public class RepositoryConfig {

    /**
     * Create a DataSource CREST DB.
     *
     * @return DataSource
     */
    @Primary
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource.main")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Create a DataSource for trigger DB.
     *
     * @return DataSource
     */
    @Bean(name = "triggerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.trigger")
    public DataSource triggerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "mainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(
            @Qualifier("dataSource") DataSource mainDataSource,
            JpaProperties jpaProperties) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(mainDataSource);
        em.setPackagesToScan("hep.crest.server"); // Adjust the package to your project structure
        em.setPersistenceUnitName("persistence.main"); // Set the persistence unit name

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.putAll(jpaProperties.getProperties());
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean(name = "mainTransactionManager")
    public PlatformTransactionManager mainTransactionManager(
            @Qualifier("mainEntityManagerFactory") LocalContainerEntityManagerFactoryBean mainEntityManagerFactory) {
        return new JpaTransactionManager(mainEntityManagerFactory.getObject());
    }

    /**
     * Create a helper bean.
     *
     * @param cprops the properties.
     * @return CrestTableNames
     */
    @Bean
    public CrestTableNames crestTableNames(@Autowired CrestProperties cprops) {
        final CrestTableNames bean = new CrestTableNames();
        // Set default schema and table name taken from properties.
        if (!"none".equals(cprops.getSchemaname())) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        return bean;
    }

    /**
     * Create a IovGroups Repository bean.
     *
     * @param mainDataSource  the DataSource
     * @param crestTableNames the helper for table names
     * @return IovGroupsCustom
     */
    @Bean
    public IovGroupsCustom iovGroupsRepository(@Qualifier("dataSource") DataSource mainDataSource,
                                               @Qualifier("crestTableNames") CrestTableNames crestTableNames) {
        final IovGroupsImpl bean = new IovGroupsImpl(mainDataSource);
        // Set default schema and table name taken from properties.
        bean.setCrestTableNames(crestTableNames);
        return bean;
    }


    /**
     * Create a repository for trigger DB data.
     *
     * @param triggerDataSource
     * @return ITriggerDb
     */
    @Bean
    public ITriggerDb triggerDbRepository(@Qualifier("triggerDataSource") DataSource triggerDataSource) {
        return new TriggerDb(triggerDataSource);
    }

}
