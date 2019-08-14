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
import hep.crest.data.repositories.TagMetaDBImpl;
import hep.crest.data.repositories.TagMetaDataBaseCustom;
import hep.crest.data.repositories.TagMetaPostgresImpl;
import hep.crest.data.repositories.TagMetaSQLITEImpl;

@Configuration
@ComponentScan("hep.crest.data.repositories")
public class RepositoryConfig {
	
	@Autowired
	private CrestProperties cprops;
	
    @Bean(name = "fstagrepository")
    public TagDirectoryImplementation tagdirectoryRepository() {
        return new TagDirectoryImplementation();
    }
    @Bean(name = "fsiovrepository")
    public IovDirectoryImplementation iovdirectoryRepository() {
        return new IovDirectoryImplementation();
    }
    @Bean(name = "fspayloadrepository")
    public PayloadDirectoryImplementation payloaddirectoryRepository() {
        return new PayloadDirectoryImplementation();
    }
    
    @Bean(name = "iovgroupsrepo")
    public IovGroupsCustom iovgroupsRepository(@Qualifier("dataSource") DataSource mainDataSource) {
    		IovGroupsImpl bean = new IovGroupsImpl(mainDataSource);
    		if (!cprops.getSchemaname().equals("none")) {
    			bean.setDefaultTablename(cprops.getSchemaname());
    		}
    		return bean;
    }

    /*
     * This block is for the moment here because I need to check how to include it only on demand
    @Profile({"prod","wildfly"})
    @Bean(name = "monitoringrepo")
    public IMonitoringRepository monitoringRepository(@Qualifier("daoDataSource") DataSource mainDataSource) {
    		IMonitoringRepository bean = new JdbcMonitoringRepository(mainDataSource);
    		return bean;
    }

    @Profile({"default","sqlite","h2","dev","mysql","postgres"})
    @Bean(name = "monitoringrepo")
    public IMonitoringRepository monitoringDefaultRepository(@Qualifier("daoDataSource") DataSource mainDataSource) {
    		IMonitoringRepository bean = new JdbcMonitoringRepository(mainDataSource);
    		return bean;
    }
    */

    @Profile({"test","default","prod","h2","wildfly","ssl","dev","mysql"})
    @Bean(name = "tagmetarepo")
    public TagMetaDataBaseCustom tagmetaDefaultRepository(@Qualifier("dataSource") DataSource mainDataSource) {
    	TagMetaDBImpl bean = new TagMetaDBImpl(mainDataSource);
		if (!cprops.getSchemaname().equals("none")) {
			bean.setDefaultTablename(cprops.getSchemaname());
		}
		return bean;
    }

    @Profile({"postgres","pgsvom"})
    @Bean(name = "tagmetarepo")
    public TagMetaDataBaseCustom tagmetaPostgresRepository(@Qualifier("dataSource") DataSource mainDataSource) {
    	TagMetaPostgresImpl bean = new TagMetaPostgresImpl(mainDataSource);
		if (!cprops.getSchemaname().equals("none")) {
			bean.setDefaultTablename(cprops.getSchemaname());
		}
		return bean;
    }
    
    @Profile({"sqlite"})
    @Bean(name = "tagmetarepo")
    public TagMetaDataBaseCustom tagmetaSqliteRepository(@Qualifier("dataSource") DataSource mainDataSource) {
    	TagMetaSQLITEImpl bean = new TagMetaSQLITEImpl(mainDataSource);
		if (!cprops.getSchemaname().equals("none")) {
			bean.setDefaultTablename(cprops.getSchemaname());
		}
		return bean;
    }

    @Profile({"test","default","prod","h2","wildfly","ssl","dev","mysql","cmsprep"})
    @Bean(name = "payloaddatadbrepo")
    public PayloadDataBaseCustom payloadDefaultRepository(@Qualifier("dataSource") DataSource mainDataSource) {
    	PayloadDataDBImpl bean = new PayloadDataDBImpl(mainDataSource);
		if (!cprops.getSchemaname().equals("none")) {
			bean.setDefaultTablename(cprops.getSchemaname());
		}
		return bean;
    }
    
    @Profile({"postgres","pgsvom"})
    @Bean(name = "payloaddatadbrepo")
    public PayloadDataBaseCustom payloadPostgresRepository(@Qualifier("dataSource") DataSource mainDataSource) {
    	PayloadDataPostgresImpl bean = new PayloadDataPostgresImpl(mainDataSource);
		if (!cprops.getSchemaname().equals("none")) {
			bean.setDefaultTablename(cprops.getSchemaname());
		}
		return bean;
    }
    
    @Profile("sqlite")
    @Bean(name = "payloaddatadbrepo")
    public PayloadDataBaseCustom payloadSqliteRepository(@Qualifier("dataSource") DataSource mainDataSource) {
    	PayloadDataSQLITEImpl bean = new PayloadDataSQLITEImpl(mainDataSource);
		if (!cprops.getSchemaname().equals("none")) {
			bean.setDefaultTablename(cprops.getSchemaname());
		}
		return bean;
    }

}
