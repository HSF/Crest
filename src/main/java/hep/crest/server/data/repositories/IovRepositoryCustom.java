package hep.crest.server.data.repositories;

import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.repositories.args.IovQueryArgs;
import hep.crest.server.exceptions.CdbNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The main method to retrieve IOVs.
 */
public interface IovRepositoryCustom {

    /**
     * General purpose IOV retrieval.
     *
     * @param args
     * @param preq
     * @return Page of Iov.
     * @throws CdbNotFoundException
     */
    Page<Iov> findIovList(IovQueryArgs args, Pageable preq) throws CdbNotFoundException;

}
