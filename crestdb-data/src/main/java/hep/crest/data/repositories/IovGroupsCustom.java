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

	List<BigDecimal> selectGroups(String tagname, Long groupsize);
	
	List<BigDecimal> selectSnapshotGroups(String tagname, Date snap, Long groupsize);
	
	Long getSize(String tagname);

	Long getSizeBySnapshot(String tagname, Date snap);
	
	List<TagSummaryDto> getTagSummaryInfo(String tagname);
	
	// Try to implement this one in the Repository
//	List<Iov> selectLatestByGroup(String tagname, Date since, Date until);
}
