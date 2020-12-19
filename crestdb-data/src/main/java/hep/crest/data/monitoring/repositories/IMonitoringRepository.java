package hep.crest.data.monitoring.repositories;

import java.util.List;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.swagger.model.PayloadTagInfoDto;

/**
 * @author formica
 *
 */
public interface IMonitoringRepository {

    /**
     * @param tagpattern
     *            the String
     * @return List<PayloadTagInfoDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    List<PayloadTagInfoDto> selectTagInfo(String tagpattern);

}
