package hep.crest.server.converters;

import hep.crest.server.data.runinfo.pojo.RunLumiInfo;
import hep.crest.server.swagger.model.RunLumiInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for run lumi info.
 */
@Mapper(uses = {CustomMapper.class},
        componentModel = "spring"
)
public interface RunLumiMapper extends GenericMapper<RunLumiInfoDto, RunLumiInfo> {

    /**
     * The instance.
     */
    RunLumiMapper INSTANCE = Mappers.getMapper(RunLumiMapper.class);

    /**
     * Convert a run lumi info to a dto.
     * @param source
     * @return RunLumiInfoDto
     */
    @Mapping(target = "runNumber", source = "source.id.runNumber")
    @Mapping(target = "lb", source = "source.id.lb")
    @Mapping(target = "starttime", source = "starttime", qualifiedByName = "bigIntToBigDecimal")
    @Mapping(target = "endtime", source = "endtime", qualifiedByName = "bigIntToBigDecimal")
    RunLumiInfoDto toDto(RunLumiInfo source);

    /**
     * Convert a dto to a run lumi info.
     * @param source
     * @return RunLumiInfo
     */
    @Mapping(target = "insertionTime", ignore = true)
    @Mapping(target = "starttime", source = "starttime", qualifiedByName = "bigDecimalToBigInt")
    @Mapping(target = "endtime", source = "endtime", qualifiedByName = "bigDecimalToBigInt")
    @Mapping(target = "id.runNumber", source = "runNumber")
    @Mapping(target = "id.lb", source = "lb")
    RunLumiInfo toEntity(RunLumiInfoDto source);
}
