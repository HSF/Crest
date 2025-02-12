package hep.crest.server.data.repositories;

import hep.crest.server.config.CrestTableNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

@Slf4j
public class DataGeneral {
    /**
     * The Data Source.
     */
    private final DataSource ds;
    /**
     * The upload directory for files.
     */
    @Value("${crest.upload.dir:/tmp}")
    private String serverUploadLocationFolder;
    /**
     * Create the utility class for table names.
     */
    private CrestTableNames crestTableNames = null;

    /**
     * @param ds
     */
    public DataGeneral(DataSource ds) {
        super();
        this.ds = ds;
    }

    /**
     * @param ctn the CrestTableNames
     * @return
     */
    public void setCrestTableNames(CrestTableNames ctn) {
        log.debug("Setting Crest table name helper to {}", ctn);
        crestTableNames = ctn;
        log.debug("Setting table names for payload and iov: {} {}", ctn.getPayloadTableName(),
                ctn.getIovTableName());
    }

    /**
     * @return DataSource
     */
    protected DataSource getDs() {
        return ds;
    }

    /**
     * @return the serverUploadLocationFolder
     */
    protected String getServerUploadLocationFolder() {
        return serverUploadLocationFolder;
    }

    /**
     * Get the crestTableNames helper.
     *
     * @return CrestTableNames the helper.
     */
    protected CrestTableNames getCrestTableNames() {
        if (this.crestTableNames == null) {
            this.crestTableNames = new CrestTableNames();
        }
        return this.crestTableNames;
    }

    /**
     * @param serverUploadLocationFolder the serverUploadLocationFolder to set
     */
    protected void setServerUploadLocationFolder(String serverUploadLocationFolder) {
        this.serverUploadLocationFolder = serverUploadLocationFolder;
    }
}
