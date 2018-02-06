package hep.crest.data.monitoring.repositories;

import java.util.List;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.swagger.model.PayloadTagInfoDto;

public interface IMonitoringRepository {

	/**
	 * @param tagpattern
	 * @return list of monitoring metrics
	 * @throws Exception
	 */
	List<PayloadTagInfoDto> selectTagInfo(String tagpattern) throws CdbServiceException;

}