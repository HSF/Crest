package hep.crest.server.controllers;

import java.io.InputStream;

/**
 * Interface for streaming output.
 *
 * @author formica
 */
public interface LobStreamerProvider {

    /**
     * @return InputStream.
     */
    InputStream getInputStream();
}
