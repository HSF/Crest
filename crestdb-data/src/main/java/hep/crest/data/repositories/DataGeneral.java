package hep.crest.data.repositories;

import hep.crest.data.config.DatabasePropertyConfigurator;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.Table;
import javax.sql.DataSource;

public class DataGeneral {
    /**
     * The Data Source.
     */
    protected final DataSource ds;
    /**
     * The upload directory for files.
     */
    @Value("${crest.upload.dir:/tmp}")
    private String serverUploadLocationFolder;
    /**
     * Default table name.
     */
    private String defaultTablename = null;

    /**
     * Table annotation to be filled by sub classes.
     */
    protected Table ann = null;

    /**
     * @param ds
     */
    public DataGeneral(DataSource ds) {
        super();
        this.ds = ds;
    }

    /**
     * @param defaultTablename the String
     * @return
     */
    public void setDefaultTablename(String defaultTablename) {
        if (this.defaultTablename == null) {
            this.defaultTablename = defaultTablename;
        }
    }

    /**
     * @return String
     */
    protected String tablename() {
        String tablename = ann.name();
        if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
            tablename = DatabasePropertyConfigurator.SCHEMA_NAME + "." + tablename;
        }
        else if (this.defaultTablename != null) {
            tablename = this.defaultTablename + "." + tablename;
        }
        return tablename;
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
     * @param serverUploadLocationFolder the serverUploadLocationFolder to set
     */
    protected void setServerUploadLocationFolder(String serverUploadLocationFolder) {
        this.serverUploadLocationFolder = serverUploadLocationFolder;
    }
}
