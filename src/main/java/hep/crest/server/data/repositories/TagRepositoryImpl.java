/**
 *
 */
package hep.crest.server.data.repositories;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import hep.crest.server.data.pojo.QTag;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.repositories.args.TagQueryArgs;
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
public class TagRepositoryImpl implements TagRepositoryCustom {
    /**
     * The entity manager.
     */
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Tag> findTagList(TagQueryArgs queryArgs, Pageable preq) throws CdbNotFoundException {
        JPQLQuery<Tag> query = new JPAQuery<>(entityManager);

        final BooleanBuilder where = new BooleanBuilder();

        log.debug("Build query from args: {}", queryArgs);
        if (queryArgs.name() != null) {
            if (queryArgs.name().contains("%")) {
                where.and(QTag.tag.name.like(queryArgs.name()));
            }
            else {
                where.and(QTag.tag.name.eq(queryArgs.name()));
            }
            log.debug("Add where condition on tag name: {}", queryArgs.name());
        }
        if (queryArgs.objectType() != null) {
            where.and(QTag.tag.objectType.like(queryArgs.objectType()));
            log.debug("Add where condition on objectType: {}", queryArgs.objectType());
        }
        if (queryArgs.description() != null) {
            where.and(QTag.tag.description.like(queryArgs.description()));
            log.debug("Add where condition on description: {}", queryArgs.description());
        }
        if (queryArgs.timeType() != null) {
            where.and(QTag.tag.timeType.like(queryArgs.timeType()));
            log.debug("Add where condition on timeType: {}", queryArgs.timeType());
        }


        // Build the query.
        log.info("Launch query for tags....");
        QTag tag = QTag.tag;
        query = query.select(tag).from(tag)
                .where(where)
                .distinct();

        log.debug("created JPQL query: {}", query.toString());

        PathBuilder<Tag> builder = (new PathBuilderFactory()).create(Tag.class);
        Querydsl querydsl = new Querydsl(entityManager, builder);
        long totalElements = query.fetchCount();
        List<Tag> rows = querydsl.applyPagination(preq, query).fetch();

        return new PageImpl<>(rows, preq, totalElements);
    }

}
