package hep.crest.data.repositories;

import hep.crest.data.pojo.Tag;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ITagQuery {

    /**
     * @param name
     *            the String
     * @return Tag
     */
    Tag findByName(@Param("name") String name);

    /**
     * @param name
     *            the String
     * @return List<Tag>
     */
    List<Tag> findByNameLike(@Param("name") String name);

}
