package hep.crest.server.services;

import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.Payload;
import hep.crest.server.data.pojo.PayloadData;
import hep.crest.server.data.pojo.PayloadInfoData;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Accessors(fluent = true)
@Data
public class StorableData {

    /**
     * The streamer Info.
     */
    private PayloadInfoData payloadInfoData;
    /**
     * The data content.
     */
    private PayloadData payloadData;
    /**
     * The metadata of the payload.
     */
    private Payload payload;
    /**
     * The iov.
     */
    private Iov iov;
    /**
     * The streams from input files.
     * Can be null in which case we have inline payload data.
     */
    private Map<String, Object> streamsMap;
}
