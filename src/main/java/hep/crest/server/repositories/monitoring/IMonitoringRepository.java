package hep.crest.server.repositories.monitoring;

import hep.crest.server.exceptions.AbstractCdbServiceException;
import hep.crest.server.swagger.model.IovPayloadDto;
import hep.crest.server.swagger.model.PayloadTagInfoDto;
import hep.crest.server.swagger.model.TagSummaryDto;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author formica
 *
 */
public interface IMonitoringRepository {

    /**
     * @param tagpattern
     *            the String
     * @return List<PayloadTagInfoDto>
     * @throws AbstractCdbServiceException
     *             If an Exception occurred
     */
    List<PayloadTagInfoDto> selectTagInfo(String tagpattern);

    /**
     * Count the iovs in a tag.
     *
     * @param tagname
     * @return List of TagSummaryDto
     */
    List<TagSummaryDto> getTagSummaryInfo(String tagname);

    /**
     * Get a list of iovs and payload meta information without retrieving the data.
     *
     * @param name
     * @param since
     * @param until
     * @param snapshot
     * @return List of IovPayloadDto
     */
    List<IovPayloadDto> getRangeIovPayloadInfo(String name, BigInteger since,
                                               BigInteger until, Date snapshot);
}
