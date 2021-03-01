package hep.crest.data.repositories;

import hep.crest.data.pojo.Tag;
import hep.crest.data.utils.DirectoryUtilities;

import java.util.List;

public interface ITagCrud extends ITagQuery {

    /**
     * Remove an iov using the Id.
     * @param id
     */
    void deleteById(String id);

    /**
     * Remove an iov using the entity.
     * @param entity
     */
    void delete(Tag entity);

    /**
     * Save an iov.
     * @param entity the Iov to save.
     * @return Tag.
     */
    Tag save(Tag entity);

    /**
     * Find all tags in the backend.
     *
     * @return List<Tag>
     */
    List<Tag> findAll();

    /**
     * @param id
     *            the String
     * @return Tag
     */
    Tag findOne(String id);

    /**
     * Set directory utilities.
     *
     * @param du
     */
    void setDirtools(DirectoryUtilities du);
}
