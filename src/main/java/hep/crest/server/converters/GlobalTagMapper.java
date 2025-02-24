package hep.crest.server.converters;

import hep.crest.server.data.pojo.GlobalTag;
import hep.crest.server.swagger.model.GlobalTagDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for global tags.
 * It takes a GlobalTag pojo and transform it to a Dto or viceversa.
 * The Offsetdatetime is using UTC offset.
 */
@Mapper(uses = CustomMapper.class,
        componentModel = "spring"
) // Use a custom mapper for date conversions
public interface GlobalTagMapper extends GenericMapper<GlobalTagDto, GlobalTag> {

    /**
     * The instance.
     */
    GlobalTagMapper INSTANCE = Mappers.getMapper(GlobalTagMapper.class);

    /**
     * Convert a global tag to a dto.
     * @param source
     * @return GlobalTagDto
     */
    @Mapping(source = "insertionTime", target = "insertionTime", qualifiedByName = "toOffsetDateTime")
    @Mapping(source = "snapshotTime", target = "snapshotTime", qualifiedByName = "toOffsetDateTime")
    @Mapping(source = "insertionTime", target = "insertionTimeMilli", qualifiedByName = "toMilli")
    @Mapping(source = "snapshotTime", target = "snapshotTimeMilli", qualifiedByName = "toMilli")
    @Mapping(source = "type", target = "type", qualifiedByName = "charToString")
    GlobalTagDto toDto(GlobalTag source);

    /**
     * Convert a dto to a global tag.
     * @param source
     * @return GlobalTag
     */
    @Mapping(target = "globalTagMaps", ignore = true)
    @Mapping(source = "insertionTime", target = "insertionTime", qualifiedByName = "toDate")
    @Mapping(source = "snapshotTime", target = "snapshotTime", qualifiedByName = "toDate")
    @Mapping(source = "type", target = "type", qualifiedByName = "stringToChar")
    GlobalTag toEntity(GlobalTagDto source);
}
