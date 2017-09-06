package hep.crest.data.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import hep.crest.data.repositories.IovDirectoryImplementation;
import hep.crest.data.repositories.IovGroupsImpl;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.PayloadDataDBImpl;
import hep.crest.data.repositories.PayloadDataSQLITEImpl;
import hep.crest.data.repositories.PayloadDirectoryImplementation;
import hep.crest.data.repositories.TagDirectoryImplementation;

@Configuration
@ComponentScan("hep.crest.data.repositories")
public class RepositoryConfig {
	
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
    public IovGroupsImpl iovgroupsRepository(@Qualifier("daoDataSource") DataSource mainDataSource) {
    		return new IovGroupsImpl(mainDataSource);
    }

    @Profile({"default","prod","h2"})
    @Bean(name = "payloaddatadbrepo")
    public PayloadDataBaseCustom payloadDefaultRepository(@Qualifier("daoDataSource") DataSource mainDataSource) {
    		return new PayloadDataDBImpl(mainDataSource);
    }
    
    @Profile("sqlite")
    @Bean(name = "payloaddatadbrepo")
    public PayloadDataBaseCustom payloadSqliteRepository(@Qualifier("daoDataSource") DataSource mainDataSource) {
    		return new PayloadDataSQLITEImpl(mainDataSource);
    }

}