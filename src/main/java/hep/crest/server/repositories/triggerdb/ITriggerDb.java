package hep.crest.server.repositories.triggerdb;

import java.io.InputStream;

public interface ITriggerDb {

    /**
     * Get trigger DB data.
     * @param components
     * @return InputStream
     */
    InputStream getTriggerDBData(UrlComponents components);

    UrlComponents parseUrl(String url);
}
