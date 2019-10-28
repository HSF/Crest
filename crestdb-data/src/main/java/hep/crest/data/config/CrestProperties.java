package hep.crest.data.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties for crest.
 * 
 * @author formica
 *
 */
@Component
@ConfigurationProperties("crest")
public class CrestProperties {

    /**
     * The directory for dumping the uploaded payloads.
     */
    private String dumpdir;
    /**
     * The schema name.
     */
    private String schemaname;
    /**
     * The api name.
     */
    private String apiname;
    /**
     * The security.
     */
    private String security;
    /**
     * The synchronization.
     */
    private String synchro;
    /**
     * The static web directory for web UI.
     */
    private String webstaticdir;
    /**
     * The authentication level.
     */
    private String authenticationtype;

    /**
     * The COOL infinity.
     */
    public static final BigDecimal INFINITY = new BigDecimal("253402297199000000000");

    /**
     * @return the dumpdir
     */
    public String getDumpdir() {
        return dumpdir;
    }

    /**
     * @param dumpdir
     *            the dumpdir to set
     */
    public void setDumpdir(String dumpdir) {
        this.dumpdir = dumpdir;
    }

    /**
     * @return the schemaname
     */
    public String getSchemaname() {
        return schemaname;
    }

    /**
     * @param schemaname
     *            the schemaname to set
     */
    public void setSchemaname(String schemaname) {
        this.schemaname = schemaname;
    }

    /**
     * @return the security
     */
    public String getSecurity() {
        return security;
    }

    /**
     * @param security
     *            the security to set
     */
    public void setSecurity(String security) {
        this.security = security;
    }

    /**
     * @return the synchro
     */
    public String getSynchro() {
        return synchro;
    }

    /**
     * @param synchro
     *            the synchro to set
     */
    public void setSynchro(String synchro) {
        this.synchro = synchro;
    }

    /**
     * @return the webstaticdir
     */
    public String getWebstaticdir() {
        return webstaticdir;
    }

    /**
     * @param webstaticdir
     *            the webstaticdir to set
     */
    public void setWebstaticdir(String webstaticdir) {
        this.webstaticdir = webstaticdir;
    }

    /**
     * @return the authenticationtype
     */
    public String getAuthenticationtype() {
        return authenticationtype;
    }

    /**
     * @param authenticationtype
     *            the authenticationtype to set
     */
    public void setAuthenticationtype(String authenticationtype) {
        this.authenticationtype = authenticationtype;
    }

    /**
     * @return the apiname
     */
    public String getApiname() {
        return apiname;
    }

    /**
     * @param apiname
     *            the apiname to set
     */
    public void setApiname(String apiname) {
        this.apiname = apiname;
    }
}
