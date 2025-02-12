/**
 *
 */
package hep.crest.server.data.repositories;

import hep.crest.server.exceptions.CdbSQLException;

import java.io.InputStream;

/**
 * Repository for Payload DATA.
 *
 * @author formica
 *
 */
public interface PayloadDataRepositoryCustom {

    /**
     * Save the data LOB.
     * @param id
     * @param is the InputStream
     * @param length the length of the stream
     * @throws CdbSQLException
     */
    void saveData(String id, InputStream is, int length) throws CdbSQLException;

    /**
     * Find the data LOB.
     * @param id
     * @return InputStream
     * @throws CdbSQLException
     */
    InputStream findData(String id) throws CdbSQLException;

    /**
     * Remove the data LOB.
     * @param id
     * @throws CdbSQLException
     */
    void deleteData(String id) throws CdbSQLException;

}
