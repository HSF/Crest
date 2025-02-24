package hep.crest.server.converters;

import hep.crest.server.data.pojo.TagMeta;
import hep.crest.server.swagger.model.TagMetaDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for tag meta.
 */
@Mapper(uses = CustomMapper.class,
        componentModel = "spring"
)
public interface TagMetaMapper extends GenericMapper<TagMetaDto, TagMeta> {

    /**
     * The instance.
     */
    TagMetaMapper INSTANCE = Mappers.getMapper(TagMetaMapper.class);

    /**
     * Convert a tag meta to a dto.
     * @param source
     * @return TagMetaDto
     */
    @Mapping(source = "insertionTime", target = "insertionTime", qualifiedByName = "toOffsetDateTime")
    @Mapping(source = "tagInfo", target = "tagInfo", qualifiedByName = "byteArrayToString")
    TagMetaDto toDto(TagMeta source);

    /**
     * Convert a dto to a tag meta.
     * @param source
     * @return TagMeta
     */
    @Mapping(target = "insertionTime", ignore = true)
    @Mapping(source = "tagInfo", target = "tagInfo", qualifiedByName = "stringToByteArray")
    TagMeta toEntity(TagMetaDto source);
}
