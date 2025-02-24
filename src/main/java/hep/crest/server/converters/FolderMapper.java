package hep.crest.server.converters;

import hep.crest.server.data.pojo.CrestFolders;
import hep.crest.server.swagger.model.FolderDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for folders.
 */
@Mapper(
        componentModel = "spring"
)
public interface FolderMapper extends GenericMapper<FolderDto, CrestFolders> {

    /**
     * The instance.
     */
    FolderMapper INSTANCE = Mappers.getMapper(FolderMapper.class);

    /**
     * Convert a folder to a dto.
     * @param source
     * @return FolderDto
     */
    FolderDto toDto(CrestFolders source);

    /**
     * Convert a dto to a folder.
     * @param source
     * @return CrestFolders
     */
    CrestFolders fromDto(FolderDto source);
}
