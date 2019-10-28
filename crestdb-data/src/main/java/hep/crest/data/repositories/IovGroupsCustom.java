/**
 * 
 */
package hep.crest.data.repositories;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import hep.crest.swagger.model.TagSummaryDto;

/**
 * Interface for groups requests.
 *
 * @author aformic
 *
 */
public interface IovGroupsCustom {

    /**
     * @param tagname
     *            the String
     * @param groupsize
     *            the Long
     * @return List<BigDecimal>
     */
    List<BigDecimal> selectGroups(String tagname, Long groupsize);

    /**
     * @param tagname
     *            the String
     * @param snap
     *            the Date
     * @param groupsize the Long
     * @return List<BigDecimal>
     */
    List<BigDecimal> selectSnapshotGroups(String tagname, Date snap, Long groupsize);

    /**
     * @param tagname
     *            the String
     * @return Long
     */
    Long getSize(String tagname);

    /**
     * @param tagname
     *            the String
     * @param snap
     *            the Date
     * @return Long
     */
    Long getSizeBySnapshot(String tagname, Date snap);

    /**
     * @param tagname
     *            the String
     * @return List<TagSummaryDto>
     */
    List<TagSummaryDto> getTagSummaryInfo(String tagname);

}
