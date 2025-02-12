package hep.crest.server.controllers;

/**
 * Abstract class for interface LobStreamProvider.
 *
 * @author formica
 */
public abstract class SimpleLobStreamerProvider implements LobStreamerProvider {

    /**
     * The key for accessing the Lob.
     */
    private final String key;

    /**
     * The source of the Lob.
     */
    private final String source;

    /**
     * Initialize the key.
     * @param key
     * @param source
     */
    protected SimpleLobStreamerProvider(String key, String source) {
        this.source = source;
        this.key = key;
    }
}
