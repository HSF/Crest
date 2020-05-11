package hep.crest.data.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import hep.crest.data.repositories.IovDirectoryImplementation;
import hep.crest.data.repositories.IovGroupsCustom;
import hep.crest.data.repositories.IovGroupsImpl;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.PayloadDataDBImpl;
import hep.crest.data.repositories.PayloadDataPostgresImpl;
import hep.crest.data.repositories.PayloadDataSQLITEImpl;
import hep.crest.data.repositories.PayloadDirectoryImplementation;
import hep.crest.data.repositories.TagDirectoryImplementation;
import hep.crest.data.utils.DirectoryUtilities;

/**
 * Repository configuration.
 * 
 * @author formica
 *
 */
@Configuration
@ComponentScan("hep.crest.data.repositories")
public class RepositoryConfig {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RepositoryConfig.class);

    /**
     * The properties.
     */
    @Autowired
    private CrestProperties cprops;

    /**
     * @return TagDirectoryImplementation
     */
    @Bean(name = "fstagrepository")
    public TagDirectoryImplementation tagdirectoryRepository() {
        final TagDirectoryImplementation tdi = new TagDirectoryImplementation();
        // Initialize directory utilities.
        final DirectoryUtilities du = new DirectoryUtilities(cprops.getDumpdir());
        tdi.setDirtools(du);
        return tdi;
    }

    /**
     * @return IovDirectoryImplementation
     */
    @Bean(name = "fsiovrepository")
    public IovDirectoryImplementation iovdirectoryRepository() {
        final IovDirectoryImplementation idi = new IovDirectoryImplementation();
        // Initialize directory utilities.
        final DirectoryUtilities du = new DirectoryUtilities(cprops.getDumpdir());
        idi.setDirtools(du);
        return idi;
    }

    /**
     * @return PayloadDirectoryImplementation
     */
    @Bean(name = "fspayloadrepository")
    public PayloadDirectoryImplementation payloaddirectoryRepository() {
        final PayloadDirectoryImplementation pdi = new PayloadDirectoryImplementation();
        // Initialize directory utilities.
        final DirectoryUtilities du = new DirectoryUtilities(cprops.getDumpdir());
        pdi.setDirtools(du);
        return pdi;
   }

    /**
     * @param mainDataSource
     *            the DataSource
     * @return IovGroupsCustom
     */
    @Bean(name = "iovgroupsrepo")
    public IovGroupsCustom iovgroupsRepository(@Qualifier("dataSource") DataSource mainDataSource) {
        final IovGroupsImpl bean = new IovGroupsImpl(mainDataSource);
        // Set default schema and table name taken from properties.
        if (!"none".equals(cprops.getSchemaname())) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        return bean;
    }

    /**
     * @param mainDataSource
     *            the DataSource
     * @return PayloadDataBaseCustom
     */
    @Profile({ "test", "default", "prod", "h2", "wildfly", "ssl", "dev", "mysql", "cmsprep",
            "oracle" })
    @Bean(name = "payloaddatadbrepo")
    public PayloadDataBaseCustom payloadDefaultRepository(
            @Qualifier("dataSource") DataSource mainDataSource) {
        final PayloadDataDBImpl bean = new PayloadDataDBImpl(mainDataSource);
        // Set default schema and table name taken from properties.
        if (!"none".equals(cprops.getSchemaname())) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        log.info("Creating default repository implementation.");
        return bean;
    }

    /**
     * @param mainDataSource
     *            the DataSource
     * @return PayloadDataBaseCustom
     */
    @Profile({ "postgres", "pgsvom" })
    @Bean(name = "payloaddatadbrepo")
    public PayloadDataBaseCustom payloadPostgresRepository(
            @Qualifier("dataSource") DataSource mainDataSource) {
        final PayloadDataPostgresImpl bean = new PayloadDataPostgresImpl(mainDataSource);
        // Set default schema and table name taken from properties.
        if (!"none".equals(cprops.getSchemaname())) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        log.info("Creating postegres repository implementation.");
        return bean;
    }

    /**
     * @param mainDataSource
     *            the DataSource
     * @return PayloadDataBaseCustom
     */
    @Profile("sqlite")
    @Bean(name = "payloaddatadbrepo")
    public PayloadDataBaseCustom payloadSqliteRepository(
            @Qualifier("dataSource") DataSource mainDataSource) {
        final PayloadDataSQLITEImpl bean = new PayloadDataSQLITEImpl(mainDataSource);
        // Set default schema and table name taken from properties.
        if (!"none".equals(cprops.getSchemaname())) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        log.info("Creating sqlite repository implementation.");
        return bean;
    }

}
