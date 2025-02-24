package hep.crest.server.converters;

import hep.crest.server.data.pojo.GlobalTag;
import hep.crest.server.data.pojo.GlobalTagMap;
import hep.crest.server.data.pojo.GlobalTagMapId;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.swagger.model.GlobalTagMapDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for global tag maps.
 */
@Mapper(uses = {GlobalTag.class, Tag.class, GlobalTagMapId.class},
        componentModel = "spring"
)
public interface GlobalTagMapMapper extends GenericMapper<GlobalTagMapDto, GlobalTagMap> {

    /**
     * The instance.
     */
    GlobalTagMapMapper INSTANCE = Mappers.getMapper(GlobalTagMapMapper.class);

    /**
     * Convert a global tag map to a dto.
     * @param source
     * @return GlobalTagMapDto
     */
    @Mapping(source = "id.globalTagName", target = "globalTagName")
    @Mapping(source = "id.tagRecord", target = "record")
    @Mapping(source = "id.label", target = "label")
    @Mapping(source = "tag.name", target = "tagName")
    GlobalTagMapDto toDto(GlobalTagMap source);

    /**
     * Convert a dto to a global tag map.
     * @param source
     * @return GlobalTagMap
     */
    @Mapping(target = "id.globalTagName", source = "globalTagName")
    @Mapping(target = "id.tagRecord", source = "record")
    @Mapping(target = "id.label", source = "label")
    @Mapping(target = "tag", expression = "java(new Tag().setName(source.getTagName()))")
    GlobalTagMap toEntity(GlobalTagMapDto source);
}
