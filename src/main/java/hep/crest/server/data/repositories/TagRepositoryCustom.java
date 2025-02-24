package hep.crest.server.data.repositories;

import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.repositories.args.TagQueryArgs;
import hep.crest.server.exceptions.CdbNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The main method to retrieve IOVs.
 */
public interface TagRepositoryCustom {

    /**
     * General purpose IOV retrieval.
     *
     * @param args
     * @param preq
     * @return Page of Tag.
     * @throws CdbNotFoundException
     */
    Page<Tag> findTagList(TagQueryArgs args, Pageable preq) throws CdbNotFoundException;

}
