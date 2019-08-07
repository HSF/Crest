package hep.crest.data.config;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Payload;
import hep.crest.data.pojo.Tag;
import hep.crest.data.runinfo.pojo.RunLumiInfo;
import hep.crest.data.security.pojo.CrestFolders;
import hep.crest.swagger.model.FolderDto;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.GlobalTagMapDto;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.TagDto;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;

@Configuration
public class PojoDtoConverterConfig {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Bean(name = "mapperFactory")
	public MapperFactory createOrikaMapperFactory() {
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		this.initGlobalTagMap(mapperFactory);
		this.initGlobalTagMapsMap(mapperFactory);
		this.initTagMap(mapperFactory);
		this.initIovMap(mapperFactory);
		this.initPayloadMap(mapperFactory);
		this.initRunLumiInfoMap(mapperFactory);
		this.initFolderMap(mapperFactory);
		return mapperFactory;
	}

	protected void initGlobalTagMap(MapperFactory mapperFactory) {
		mapperFactory.classMap(GlobalTag.class, GlobalTagDto.class).exclude("globalTagMaps").byDefault().register();
	}

	protected void initGlobalTagMapsMap(MapperFactory mapperFactory) {
		mapperFactory.classMap(GlobalTagMap.class, GlobalTagMapDto.class).field("id.globalTagName", "globalTagName")
				.field("id.record", "record").field("id.label", "label").field("tag.name", "tagName").register();
	}

	protected void initTagMap(MapperFactory mapperFactory) {
		mapperFactory.classMap(Tag.class, TagDto.class)
			.field("objectType", "payloadSpec")
			.exclude("globalTagMaps").byDefault().register();
	}


	protected void initIovMap(MapperFactory mapperFactory) {
		mapperFactory.classMap(Iov.class, IovDto.class).field("id.tagName", "tagName").field("id.since", "since")
				.field("id.insertionTime", "insertionTime").field("payloadHash", "payloadHash").register();
	}

	protected void initPayloadMap(MapperFactory mapperFactory) {
		mapperFactory.classMap(Payload.class, PayloadDto.class)
		.byDefault()
		.customize(new CustomMapper<Payload, PayloadDto>() {
			@Override
			public void mapAtoB(Payload a, PayloadDto b, MappingContext context) {
				try {
					b.hash(a.getHash())
					.version(a.getVersion())
					.objectType(a.getObjectType())
					.data(a.getData().getBytes(1, (int) a.getData().length()))
					.streamerInfo(a.getStreamerInfo().getBytes(1, (int) a.getStreamerInfo().length()))
					.size(a.getSize())
					.insertionTime(a.getInsertionTime());
				} catch (SQLException e) {
					log.error("SQL exception in mapping pojo and dto for payload...: {}",e.getMessage());
				}
			}	
		})
		.register();
	}

	protected void initRunLumiInfoMap(MapperFactory mapperFactory) {
		mapperFactory.classMap(RunLumiInfo.class, RunLumiInfoDto.class).exclude("insertionTime").byDefault().register();
	}

	protected void initFolderMap(MapperFactory mapperFactory) {
		mapperFactory.classMap(CrestFolders.class, FolderDto.class).byDefault().register();
	}

	@Bean(name = "mapper")
	@Autowired
	public MapperFacade createMapperFacade(@Qualifier("mapperFactory") MapperFactory mapperFactory) {
		return mapperFactory.getMapperFacade();
	}
}