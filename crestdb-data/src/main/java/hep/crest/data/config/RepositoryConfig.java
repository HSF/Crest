package hep.crest.data.config;

import javax.sql.DataSource;

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
     * The properties.
     */
    @Autowired
    private CrestProperties cprops;

    /**
     * @return TagDirectoryImplementation
     */
    @Bean(name = "fstagrepository")
    public TagDirectoryImplementation tagdirectoryRepository() {
        return new TagDirectoryImplementation();
    }

    /**
     * @return IovDirectoryImplementation
     */
    @Bean(name = "fsiovrepository")
    public IovDirectoryImplementation iovdirectoryRepository() {
        return new IovDirectoryImplementation();
    }

    /**
     * @return PayloadDirectoryImplementation
     */
    @Bean(name = "fspayloadrepository")
    public PayloadDirectoryImplementation payloaddirectoryRepository() {
        return new PayloadDirectoryImplementation();
    }

    /**
     * @param mainDataSource
     *            the DataSource
     * @return IovGroupsCustom
     */
    @Bean(name = "iovgroupsrepo")
    public IovGroupsCustom iovgroupsRepository(@Qualifier("dataSource") DataSource mainDataSource) {
        final IovGroupsImpl bean = new IovGroupsImpl(mainDataSource);
        if (!cprops.getSchemaname().equals("none")) {
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
        if (!cprops.getSchemaname().equals("none")) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
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
        if (!cprops.getSchemaname().equals("none")) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
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
        if (!cprops.getSchemaname().equals("none")) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        return bean;
    }
}
