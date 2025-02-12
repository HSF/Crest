/**
 *
 */
package hep.crest.server;

import hep.crest.server.exceptions.CdbBadRequestException;
import hep.crest.server.exceptions.CdbInternalException;
import hep.crest.server.exceptions.CdbNotFoundException;
import hep.crest.server.exceptions.CdbSQLException;
import hep.crest.server.exceptions.ConflictException;
import hep.crest.server.exceptions.PayloadEncodingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author formica
 *
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExceptionsTests {

    private static final Logger log = LoggerFactory.getLogger(ExceptionsTests.class);

    @Test
    public void testExceptions() throws Exception {
        CdbSQLException sql = new CdbSQLException("Error in sql request");
        assertThat(sql.getResponseStatus()).isEqualTo(Response.Status.NOT_MODIFIED);
        assertThat(sql.getMessage()).contains("SQL");

        CdbNotFoundException notfound = new CdbNotFoundException("Entity not found");
        assertThat(notfound.getResponseStatus()).isEqualTo(Response.Status.NOT_FOUND);
        assertThat(notfound.getMessage()).contains("Not Found");

        CdbBadRequestException br = new CdbBadRequestException("Bad request");
        assertThat(br.getResponseStatus()).isEqualTo(Response.Status.BAD_REQUEST);
        assertThat(br.getMessage()).contains("Bad request");

        ConflictException criteria = new ConflictException("Conflict in request");
        assertThat(criteria.getResponseStatus()).isEqualTo(Response.Status.CONFLICT);
        assertThat(criteria.getMessage()).contains("Conflict");

        CdbInternalException rf = new CdbInternalException("Service internal error");
        assertThat(rf.getResponseStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
        assertThat(rf.getMessage()).contains("Internal");

        PayloadEncodingException badr = new PayloadEncodingException("Some encoding error",
                null);
        assertThat(badr.getResponseStatus()).isEqualTo(Response.Status.NOT_ACCEPTABLE);
        assertThat(badr.getMessage()).contains("Encoding");
    }

    @Test
    public void testExceptionsWithReThrow() throws Exception {
        RuntimeException e = new RuntimeException("some runtime");
        CdbSQLException sql = new CdbSQLException("Error in sql request", e);
        assertThat(sql.getResponseStatus()).isEqualTo(Response.Status.NOT_MODIFIED);
        assertThat(sql.getMessage()).contains("SQL");

        CdbNotFoundException notfound = new CdbNotFoundException("Entity not found", e);
        assertThat(notfound.getResponseStatus()).isEqualTo(Response.Status.NOT_FOUND);
        assertThat(notfound.getMessage()).contains("Not Found");

        ConflictException criteria = new ConflictException("Criteria not correct", e);
        assertThat(criteria.getResponseStatus()).isEqualTo(Response.Status.CONFLICT);
        assertThat(criteria.getMessage()).contains("Criteria");

        CdbInternalException serv = new CdbInternalException("Some service error", e);
        assertThat(serv.getResponseStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
        assertThat(serv.getMessage()).contains("Internal");

        CdbBadRequestException br = new CdbBadRequestException("Bad request", e);
        assertThat(br.getResponseStatus()).isEqualTo(Response.Status.BAD_REQUEST);
        assertThat(br.getMessage()).contains("Bad request");

        PayloadEncodingException pyld = new PayloadEncodingException("Some error in payload", e);
        assertThat(pyld.getResponseStatus()).isEqualTo(Response.Status.NOT_ACCEPTABLE);
        assertThat(pyld.getMessage()).contains("Encoding");
    }

    @Test
    public void testExceptionsWithReThrowOnly() throws Exception {
        RuntimeException e = new RuntimeException("some runtime");
        CdbSQLException sql = new CdbSQLException(e);
        assertThat(sql.getResponseStatus()).isEqualTo(Response.Status.NOT_MODIFIED);
        assertThat(sql.getMessage()).contains("SQL");

        CdbNotFoundException notfound = new CdbNotFoundException(e);
        assertThat(notfound.getResponseStatus()).isEqualTo(Response.Status.NOT_FOUND);
        assertThat(notfound.getMessage()).contains("Not Found");

        CdbInternalException serv = new CdbInternalException(e);
        assertThat(serv.getResponseStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
        assertThat(serv.getMessage()).contains("Internal");

        ConflictException rf = new ConflictException(e);
        assertThat(rf.getResponseStatus()).isEqualTo(Response.Status.CONFLICT);
        assertThat(rf.getMessage()).contains("Conflict");

        CdbBadRequestException br = new CdbBadRequestException(e);
        assertThat(br.getResponseStatus()).isEqualTo(Response.Status.BAD_REQUEST);
        assertThat(br.getMessage()).contains("Bad request");

        PayloadEncodingException pyld = new PayloadEncodingException(e);
        assertThat(pyld.getResponseStatus()).isEqualTo(Response.Status.NOT_ACCEPTABLE);
        assertThat(pyld.getMessage()).contains("Encoding");
    }

}
