package hep.crest.data.config;

import hep.crest.data.repositories.TagMetaDBImpl;
import hep.crest.data.repositories.TagMetaDataBaseCustom;
import hep.crest.data.repositories.TagMetaPostgresImpl;
import hep.crest.data.repositories.TagMetaSQLITEImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Repository configuration.
 * 
 * @author formica
 *
 */
@Configuration
@ComponentScan("hep.crest.data.repositories")
public class AtlasRepositoryConfig {

    /**
     * The properties.
     */
    @Autowired
    private CrestProperties cprops;

    /**
     * @param mainDataSource
     *            the DataSource
     * @return TagMetaDataBaseCustom
     */
    @Profile({ "test", "default", "prod", "h2", "oracle", "wildfly", "ssl", "dev", "mysql" })
    @Bean(name = "tagmetarepo")
    public TagMetaDataBaseCustom tagmetaDefaultRepository(
            @Qualifier("dataSource") DataSource mainDataSource) {
        final TagMetaDBImpl bean = new TagMetaDBImpl(mainDataSource);
        if (!"none".equals(cprops.getSchemaname())) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        return bean;
    }

    /**
     * @param mainDataSource
     *            the DataSource
     * @return TagMetaDataBaseCustom
     */
    @Profile({ "postgres" })
    @Bean(name = "tagmetarepo")
    public TagMetaDataBaseCustom tagmetaPostgresRepository(
            @Qualifier("dataSource") DataSource mainDataSource) {
        final TagMetaPostgresImpl bean = new TagMetaPostgresImpl(mainDataSource);
        if (!"none".equals(cprops.getSchemaname())) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        return bean;
    }

    /**
     * @param mainDataSource
     *            the DataSource
     * @return TagMetaDataBaseCustom
     */
    @Profile({ "sqlite" })
    @Bean(name = "tagmetarepo")
    public TagMetaDataBaseCustom tagmetaSqliteRepository(
            @Qualifier("dataSource") DataSource mainDataSource) {
        final TagMetaSQLITEImpl bean = new TagMetaSQLITEImpl(mainDataSource);
        if (!"none".equals(cprops.getSchemaname())) {
            bean.setDefaultTablename(cprops.getSchemaname());
        }
        return bean;
    }
}
