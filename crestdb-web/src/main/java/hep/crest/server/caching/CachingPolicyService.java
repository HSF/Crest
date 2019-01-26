package hep.crest.server.caching;

import java.math.BigDecimal;
import java.util.Date;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hep.crest.data.config.CrestProperties;
import hep.crest.swagger.model.TagDto;


@Component
public class CachingPolicyService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CachingProperties cprops;

	public CacheControl getGroupsCacheControl(Long snapshot) {
		Integer maxage = cprops.default_cache_time;
		if (snapshot != 0L) {
			maxage = cprops.getIovsgroups_snapshot_maxage();
		}
		CacheControl cc = new CacheControl();
		cc.setMaxAge(maxage);
		return cc;
	}

	public CacheControl getIovsCacheControlForUntil(Long snapshot, BigDecimal until) {
		Integer maxage = cprops.default_cache_time;
		if (!until.equals(CrestProperties.INFINITY)) {
			if (snapshot != 0L) {
				maxage = cprops.getIovs_snapshot_maxage();
			} else {
				maxage = cprops.getIovs_maxage();
			}
		}
		CacheControl cc = new CacheControl();
		cc.setMaxAge(maxage);
		return cc;
	}

	public ResponseBuilder verifyLastModified(Request request, TagDto tagentity) {
		Date lastModified = tagentity.getModificationTime();
		log.debug("Use tag modification time {}",lastModified);
		ResponseBuilder builder = request.evaluatePreconditions(lastModified);
		if (builder != null) {
			CacheControl cc = new CacheControl();
			builder.cacheControl(cc).header("Last-Modified", lastModified); // add
																		    // metadata
		}
		return builder;
	}

}
