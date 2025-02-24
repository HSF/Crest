/**
 *
 */
package hep.crest.server.data.repositories;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import hep.crest.server.data.pojo.Payload;
import hep.crest.server.data.pojo.QPayload;
import hep.crest.server.data.repositories.args.PayloadQueryArgs;
import hep.crest.server.exceptions.CdbNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * Repository for IOVs.
 *
 * @author formica
 *
 */
@Slf4j
public class PayloadRepositoryImpl implements PayloadRepositoryCustom {
    /**
     * The entity manager.
     */
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Payload> findPayloadsList(PayloadQueryArgs queryArgs, Pageable preq) throws CdbNotFoundException {
        JPQLQuery<Payload> query = new JPAQuery<>(entityManager);

        final BooleanBuilder where = new BooleanBuilder();

        log.debug("Build query from args: {}", queryArgs);
        if (queryArgs.hash() != null) {
            if (queryArgs.hash().contains("%")) {
                where.and(QPayload.payload.hash.like(queryArgs.hash()));
            }
            else {
                where.and(QPayload.payload.hash.eq(queryArgs.hash()));
            }
            log.debug("Add where condition on hash name: {}", queryArgs.hash());
        }
        if (queryArgs.objectType() != null) {
            where.and(QPayload.payload.objectType.like(queryArgs.objectType()));
            log.debug("Add where condition on objectType: {}", queryArgs.objectType());
        }
        if (queryArgs.size() != null) {
            where.and(QPayload.payload.size.goe(queryArgs.size()));
            log.debug("Add where condition on size: {}", queryArgs.size());
        }

        // Build the query.
        log.info("Launch query for payloads....");
        QPayload payload = QPayload.payload;
        query = query.select(payload).from(payload)
                .where(where)
                .distinct();

        log.debug("created JPQL query: {}", query.toString());

        PathBuilder<Payload> builder = (new PathBuilderFactory()).create(Payload.class);
        Querydsl querydsl = new Querydsl(entityManager, builder);
        long totalElements = query.fetchCount();
        List<Payload> rows = querydsl.applyPagination(preq, query).fetch();

        return new PageImpl<>(rows, preq, totalElements);
    }

}
