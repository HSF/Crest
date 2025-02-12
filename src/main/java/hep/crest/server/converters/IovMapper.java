package hep.crest.server.converters;

import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.IovId;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.swagger.model.IovDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for iovs.
 */
@Mapper(uses = {Tag.class, IovId.class, CustomMapper.class},
        componentModel = "spring"
)
public interface IovMapper extends GenericMapper<IovDto, Iov> {

    /**
     * The instance.
     */
    IovMapper INSTANCE = Mappers.getMapper(IovMapper.class);

    /**
     * Convert an iov to a dto.
     * @param source
     * @return IovDto
     */
    @Mapping(source = "id.insertionTime", target = "insertionTime", qualifiedByName =
            "toOffsetDateTime")
    @Mapping(source = "id.since", target = "since", qualifiedByName = "bigIntToBigDecimal")
    @Mapping(source = "id.tagName", target = "tagName")
    IovDto toDto(Iov source);

    /**
     * Convert a dto to an iov.
     * @param source
     * @return Iov
     */
    @Mapping(source = "since", target = "id.since", qualifiedByName = "bigDecimalToBigInt")
    @Mapping(source = "tagName", target = "id.tagName")
    @Mapping(source = "insertionTime", target = "id.insertionTime", qualifiedByName = "toDate")
    @Mapping(target = "tag", expression = "java("
            + "new hep.crest.server.data.pojo.Tag().setName(source.getTagName()))")
    Iov toEntity(IovDto source);

}
