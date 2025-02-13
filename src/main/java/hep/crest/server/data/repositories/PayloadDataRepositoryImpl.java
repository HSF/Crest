/**
 *
 */
package hep.crest.server.data.repositories;

import hep.crest.server.config.CrestTableNames;
import hep.crest.server.data.pojo.PayloadData;
import hep.crest.server.exceptions.CdbSQLException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.BlobProxy;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository for Payload DATA.
 *
 * @author formica
 */
@Repository
@Slf4j
public class PayloadDataRepositoryImpl implements PayloadDataRepositoryCustom {

    /**
     * The entity manager.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Helper.
     */
    private CrestTableNames crestTableNames;

    /**
     * Ctor for injection.
     * @param crestTableNames
     */
    @Autowired
    public PayloadDataRepositoryImpl(CrestTableNames crestTableNames) {
        this.crestTableNames = crestTableNames;
    }


    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void saveData(String id, InputStream is, int length) throws CdbSQLException {
        PayloadData entity = new PayloadData();
        entity.setHash(id);
        entity.setData(BlobProxy.generateProxy(is, length));
        entityManager.persist(entity);
    }

    @Override
    public InputStream findData(String id) throws CdbSQLException {
        try {
            // It is important that this method does not apply any transaction.
            // All existing transactions should be handled externally.
            PayloadData entity = entityManager.find(PayloadData.class, id);
            return entity.getData().getBinaryStream();
        }
        catch (SQLException e) {
            log.error("Cannot get payload data for " + id);
            throw new CdbSQLException(e);
        }
    }

    @Override
    @Transactional
    public void deleteData(String id) throws CdbSQLException {
        log.trace("Method does not do anything except for postgres....");
        final Session session = (Session) entityManager.getDelegate();
        final SessionFactoryImpl sessionFactory = (SessionFactoryImpl) session.getSessionFactory();
        final Dialect dialect = sessionFactory.getJdbcServices().getDialect();
        //<-- compare against the expected dailect classes
        if (dialect instanceof org.hibernate.dialect.PostgreSQLDialect) {
            //your database is postgres
            String tablename = crestTableNames.getPayloadDataTableName();
            String delStatement = "SELECT lo_unlink(d.data) as n FROM " + tablename
                    + " d WHERE d.hash = :id";
            List<Integer> reslist =
                    session.createNativeQuery(delStatement, Integer.class)
                            .setParameter("id", id)
                            .addScalar("n", StandardBasicTypes.INTEGER).getResultList();
            log.debug("Postgres deletion : {}", reslist.get(0));
        }
    }
}
