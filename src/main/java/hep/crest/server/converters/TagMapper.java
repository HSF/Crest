package hep.crest.server.converters;

import hep.crest.server.data.pojo.Tag;
import hep.crest.server.swagger.model.TagDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for tags.
 */
@Mapper(uses = CustomMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public interface TagMapper extends GenericMapper<TagDto, Tag> {

    /**
     * The instance.
     */
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    /**
     * Convert a tag to a dto.
     * @param source
     * @return TagDto
     */
    @Mapping(source = "objectType", target = "payloadSpec")
    @Mapping(source = "insertionTime", target = "insertionTime", qualifiedByName = "toOffsetDateTime")
    @Mapping(source = "modificationTime", target = "modificationTime", qualifiedByName = "toOffsetDateTime")
    TagDto toDto(Tag source);

    /**
     * Convert a dto to a tag.
     * @param source
     * @return Tag
     */
    @Mapping(target = "objectType", source = "payloadSpec")
    @Mapping(target = "insertionTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    Tag toEntity(TagDto source);
}
