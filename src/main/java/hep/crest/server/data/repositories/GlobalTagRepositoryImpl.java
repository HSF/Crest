/**
 *
 */
package hep.crest.server.data.repositories;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import hep.crest.server.data.pojo.GlobalTag;
import hep.crest.server.data.pojo.QGlobalTag;
import hep.crest.server.data.pojo.QGlobalTagMap;
import hep.crest.server.data.pojo.QTag;
import hep.crest.server.data.repositories.args.GtagQueryArgs;
import hep.crest.server.exceptions.CdbNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

/**
 * Repository for IOVs.
 *
 * @author formica
 *
 */
@Slf4j
public class GlobalTagRepositoryImpl implements GlobalTagRepositoryCustom {
    /**
     * The entity manager.
     */
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<GlobalTag> findGlobalTagList(GtagQueryArgs queryArgs, Pageable preq) throws CdbNotFoundException {
        JPQLQuery<GlobalTag> query = new JPAQuery<>(entityManager);

        final BooleanBuilder where = new BooleanBuilder();

        log.debug("Build query from args: {}", queryArgs);
        if (queryArgs.name() != null) {
            if (queryArgs.name().contains("%")) {
                where.and(QGlobalTag.globalTag.name.like(queryArgs.name()));
            }
            else {
                where.and(QGlobalTag.globalTag.name.eq(queryArgs.name()));
            }
            log.debug("Add where condition on globaltag name: {}", queryArgs.name());
        }
        if (queryArgs.scenario() != null) {
            where.and(QGlobalTag.globalTag.scenario.like(queryArgs.scenario()));
            log.debug("Add where condition on scenario: {}", queryArgs.scenario());
        }
        if (queryArgs.description() != null) {
            where.and(QGlobalTag.globalTag.description.like(queryArgs.description()));
            log.debug("Add where condition on description: {}", queryArgs.description());
        }
        if (queryArgs.workflow() != null) {
            where.and(QGlobalTag.globalTag.workflow.like(queryArgs.workflow()));
            log.debug("Add where condition on workflow: {}", queryArgs.workflow());
        }
        if (queryArgs.release() != null) {
            where.and(QGlobalTag.globalTag.release.like(queryArgs.release()));
            log.debug("Add where condition on release: {}", queryArgs.release());
        }
        if (queryArgs.type() != null) {
            where.and(QGlobalTag.globalTag.type.eq(queryArgs.type().charAt(0)));
            log.debug("Add where condition on type: {}", queryArgs.type());
        }
        if (queryArgs.snapshotTime() != null) {
            where.and(QGlobalTag.globalTag.snapshotTime.loe(queryArgs.snapshotTime()));
            log.debug("Add where condition on <= snapshot: {}", queryArgs.snapshotTime());
        }
        if (queryArgs.validity() != null) {
            where.and(QGlobalTag.globalTag.validity.goe(queryArgs.validity()));
            log.debug("Add where condition on >= validity: {}", queryArgs.validity());
        }

        // Build the query.
        log.info("Launch query for global tags....");
        QGlobalTag globalTag = QGlobalTag.globalTag;
        query = query.select(globalTag).from(globalTag)
                .where(where)
                .distinct();

        log.debug("created JPQL query: {}", query.toString());
        PathBuilder<GlobalTag> builder = (new PathBuilderFactory()).create(GlobalTag.class);
        Querydsl querydsl = new Querydsl(entityManager, builder);
        log.debug("Fetching total elements count...");
        long totalElements = query.fetchCount();
        log.debug("Total elements: {}", totalElements);
        log.debug("Applying pagination using {}", preq.toString());
        List<GlobalTag> rows = querydsl.applyPagination(preq, query).fetch();
        log.debug("Query retrieved {}", rows);
        return new PageImpl<>(rows, preq, totalElements);
    }

    @Override
    public Optional<GlobalTag> findGlobalTagFetchTags(String name, String mrecord, String label)
            throws CdbNotFoundException {
        JPQLQuery<GlobalTag> query = new JPAQuery<>(entityManager);

        final BooleanBuilder where = new BooleanBuilder();

        log.debug("Build query from name: {}", name);
        if (name != null) {
            where.and(QGlobalTag.globalTag.name.eq(name));
            log.debug("Add where condition on globaltag name: {}", name);
        }
        if (mrecord != null) {
            where.and(QGlobalTagMap.globalTagMap.id.tagRecord.eq(mrecord));
            log.debug("Add where condition on globaltag map record: {}", mrecord);
        }
        if (label != null) {
            where.and(QGlobalTagMap.globalTagMap.id.label.eq(label));
            log.debug("Add where condition on globaltag map label: {}", label);
        }

        QGlobalTag globalTag = QGlobalTag.globalTag;
        QGlobalTagMap globalTagMap = new QGlobalTagMap("globalTagMap");
        QTag tag = new QTag("tag");

        GlobalTag gt = query.select(globalTag)
                .from(globalTag)
                .leftJoin(globalTag.globalTagMaps, globalTagMap)
                .fetchJoin()
                .leftJoin(globalTagMap.tag, tag)
                .fetchJoin()
                .where(where).distinct().fetchOne();
        return Optional.ofNullable(gt);
    }
}
