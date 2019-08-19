/**
 * 
 */
package hep.crest.data.repositories;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import hep.crest.swagger.model.TagSummaryDto;

/**
 * @author aformic
 *
 */
public interface IovGroupsCustom {

	List<BigDecimal> selectGroups(Long tagid, Long groupsize);
	
	List<BigDecimal> selectSnapshotGroups(Long tagid, Date snap, Long groupsize);
	
	Long getSize(Long tagid);

	Long getSizeBySnapshot(Long tagid, Date snap);
	
	List<TagSummaryDto> getTagSummaryInfo(String tagname);
	
}
