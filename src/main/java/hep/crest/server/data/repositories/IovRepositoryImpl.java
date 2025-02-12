/**
 *
 */
package hep.crest.server.data.repositories;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.QIov;
import hep.crest.server.data.repositories.args.IovModeEnum;
import hep.crest.server.data.repositories.args.IovQueryArgs;
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
public class IovRepositoryImpl implements IovRepositoryCustom {
    /**
     * The entity manager.
     */
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Iov> findIovList(IovQueryArgs queryArgs, Pageable preq) throws CdbNotFoundException {
        JPQLQuery<Iov> query = new JPAQuery<>(entityManager);

        final BooleanBuilder where = new BooleanBuilder();
        final BooleanBuilder subwhere = new BooleanBuilder();

        log.debug("Build query from args: {}", queryArgs);
        if (queryArgs.hash() != null) {
            where.and(QIov.iov.payloadHash.eq(queryArgs.hash()));
            log.debug("Add where condition on hash: {}", queryArgs.hash());
        }
        if (queryArgs.tagName() != null) {
            if (queryArgs.tagName().contains("%")) {
                where.and(QIov.iov.id.tagName.like(queryArgs.tagName()));
            }
            else {
                where.and(QIov.iov.id.tagName.eq(queryArgs.tagName()));
            }
            subwhere.and(QIov.iov.id.tagName.eq(queryArgs.tagName()));
            log.debug("Add where condition on tagName: {}", queryArgs.tagName());
        }
        if (queryArgs.since() != null) {
            if (!queryArgs.mode().equals(IovModeEnum.AT)) {
                where.and(QIov.iov.id.since.goe(queryArgs.since()));
                log.debug("Add where condition on >= since: {}", queryArgs.since());
            }
            else {
                subwhere.and(QIov.iov.id.since.loe(queryArgs.since()));
                log.debug("Add where condition on <=since for AT query: {}", queryArgs.since());
            }
        }
        if (queryArgs.until() != null) {
            if (queryArgs.mode().equals(IovModeEnum.RANGES)) {
                /**
                 * In the cases of a RANGE mode, we include the last SINCE.
                 */
                where.and(QIov.iov.id.since.loe(queryArgs.until()));
            }
            else {
                /**
                 * This method is like the Range method, but it does not include the IOV before the given since.
                 * It will provide the same result as getRange only if the since time provided is equivalent
                 * to the first since selected in the DB. For other cases it will not contain the first IOV.
                 */
                where.and(QIov.iov.id.since.lt(queryArgs.until()));
            }
            log.debug("Add where condition on < until: {}", queryArgs.until());
        }
        if (queryArgs.snapshot() != null) {
            where.and(QIov.iov.id.insertionTime.loe(queryArgs.snapshot()));
            subwhere.and(QIov.iov.id.insertionTime.loe(queryArgs.snapshot()));
            log.debug("Add where condition on <= snapshot: {}", queryArgs.snapshot());
        }

        if (queryArgs.mode().equals(IovModeEnum.AT)) {
            /**
             * In the cases of a AT mode, the SINCE before the provided one should be chosen.
             */
            log.warn("Adding where condition on MAX of since");
            JPQLQuery<Iov> subquery = new JPAQuery<>(entityManager);
            where.and(QIov.iov.id.since.eq(
                    subquery.select(QIov.iov.id.since.max())
                            .from(QIov.iov).where(subwhere).fetchOne()));
        }
        else if (queryArgs.mode().equals(IovModeEnum.RANGES)) {
            /**
             * In the cases of a RANGE mode, we include the SINCE before the provided one.
             */
            log.warn("Adding where condition on MAX of since");
            JPQLQuery<Iov> subquery = new JPAQuery<>(entityManager);
            where.and(QIov.iov.id.since.goe(
                    subquery.select(QIov.iov.id.since.max())
                            .from(QIov.iov).where(subwhere).fetchOne()));

        }
        // Build the query.
        log.info("Launch query for iovs....");
        QIov iov = QIov.iov;
        // Here we did remove "distinct()" because it does not work with multiple columns.
        query = query.select(iov)
                .from(iov)
                .where(where);

        log.debug("created JPQL query: {}", query.toString());

        PathBuilder<Iov> builder = (new PathBuilderFactory()).create(Iov.class);
        Querydsl querydsl = new Querydsl(entityManager, builder);
        long totalElements = query.fetchCount();
        List<Iov> rows = querydsl.applyPagination(preq, query).fetch();

        return new PageImpl<>(rows, preq, totalElements);
    }

}
