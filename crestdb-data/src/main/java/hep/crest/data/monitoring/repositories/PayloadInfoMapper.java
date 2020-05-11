package hep.crest.data.monitoring.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import hep.crest.swagger.model.PayloadTagInfoDto;

/**
 * Class to map the result of a request into a Dto for PayloadTagInfo.
 * @version %I%, %G% 
 * @author formica
 */
public class PayloadInfoMapper implements RowMapper<PayloadTagInfoDto> {

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
     */
    @Override
    public PayloadTagInfoDto mapRow(ResultSet rs, int rownum) throws SQLException {
        final PayloadTagInfoDto bi = new PayloadTagInfoDto();
        // Fill the dto
        bi.setTagname(rs.getString("tag_name"));
        bi.setNiovs(rs.getInt("niovs"));
        bi.setTotvolume(rs.getFloat("tot_volume"));
        bi.setAvgvolume(rs.getFloat("avg_volume"));
        return bi;
    }
}
