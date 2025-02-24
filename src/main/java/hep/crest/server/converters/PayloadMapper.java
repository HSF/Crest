package hep.crest.server.converters;

import hep.crest.server.data.pojo.Payload;
import hep.crest.server.swagger.model.PayloadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for payloads.
 */
@Mapper(uses = CustomMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public interface PayloadMapper extends GenericMapper<PayloadDto, Payload> {

    /**
     * The instance.
     */
    PayloadMapper INSTANCE = Mappers.getMapper(PayloadMapper.class);

    /**
     * Convert a tag to a dto.
     * @param source
     * @return PayloadDto
     */
    @Mapping(source = "insertionTime", target = "insertionTime", qualifiedByName = "toOffsetDateTime")
    PayloadDto toDto(Payload source);

    /**
     * Convert a dto to a tag.
     * @param source
     * @return Payload
     */
    @Mapping(source = "insertionTime", target = "insertionTime", qualifiedByName = "toDate")
    Payload toEntity(PayloadDto source);
}
