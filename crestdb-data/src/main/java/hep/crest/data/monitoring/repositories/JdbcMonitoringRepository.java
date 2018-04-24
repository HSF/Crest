/**
 * 
 */
package hep.crest.data.monitoring.repositories;

import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.swagger.model.PayloadTagInfoDto;


/**
 * @author formica
 *
 */
@Component
public class JdbcMonitoringRepository implements IMonitoringRepository {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DataSource ds;


	/**
	 * 
	 */
	public JdbcMonitoringRepository() {
		// TODO Auto-generated constructor stub
	}

	
	/* (non-Javadoc)
	 * @see hep.crest.data.monitoring.repositories.IMonitoringRepository#selectTagInfo(java.lang.String)
	 */
	@Override
	public List<PayloadTagInfoDto> selectTagInfo(String tagpattern) throws CdbServiceException {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String sql;
		try {
			sql = "select tag_name, niovs, tot_volume, avg_volume from table (CREST_TOOLS.F_GETTAGSUMMARY(?))";
			log.debug("Execute query " + sql + " using " + tagpattern);
			return jdbcTemplate.query(sql, new Object[] { tagpattern }, new PayloadInfoMapper());
		} catch (EmptyResultDataAccessException emptyResultDataAccessException) {
			throw new CdbServiceException(emptyResultDataAccessException.getMessage());
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	} 

}
