package hep.crest.server.converters;

/**
 * Generic mapper interface.
 * @param <D>
 * @param <T>
 */
public interface GenericMapper<D, T> {
    /**
     * Convert an entity to a dto.
     * @param entity
     * @return D
     */
    D toDto(T entity);

    /**
     * Convert a dto to an entity.
     * @param dto
     * @return T
     */
    T toEntity(D dto);
}
