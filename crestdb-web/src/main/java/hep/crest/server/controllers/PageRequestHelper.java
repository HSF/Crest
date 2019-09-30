/**
 * 
 */
package hep.crest.server.controllers;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import hep.crest.data.repositories.querydsl.SearchCriteria;

/**
 * @author formica
 *
 */
@Component
public class PageRequestHelper {

	private static final String QRY_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:|<|>)([a-zA-Z0-9_\\-\\/\\.]+?),";
	private static final String SORT_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:)([ASC|DESC]+?),";

	private static final Integer MAX_PAGE_SIZE = 10000;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PageRequestHelper() {
		super();
	}

	/**
	 * @param page
	 * @param size
	 * @param sort
	 * @return
	 */
	public PageRequest createPageRequest(Integer page, Integer size, String sort) {

		if (size > MAX_PAGE_SIZE) {
			log.warn("Requested size exceed maximum page size...change it to {}",MAX_PAGE_SIZE);
			size = MAX_PAGE_SIZE;
		}
		Pattern sortpattern = Pattern.compile(SORT_PATTERN);
		Matcher sortmatcher = sortpattern.matcher(sort + ",");
		List<Order> orderlist = new ArrayList<>();
		while (sortmatcher.find()) {
			Direction direc = Direction.ASC;
			if (sortmatcher.group(3).equals("DESC")) {
				direc = Direction.DESC;
			}
			String field = sortmatcher.group(1);
			log.debug("Creating new order: {} {}", direc, field);
			orderlist.add(new Order(direc, field));
		}
		log.debug("Created list of sorting orders of size {}", orderlist.size());
		Order orders[] = new Order[orderlist.size()];
		int i = 0;
		for (Order order : orderlist) {
			log.debug("Order @ {} = {} ", i, order);
			orders[i++] = order;
		}
		Sort msort = Sort.by(orders);
		return PageRequest.of(page, size, msort);
	}

	/**
	 * @param by
	 * @return
	 */
	public List<SearchCriteria> createMatcherCriteria(String by) {

		Pattern pattern = Pattern.compile(QRY_PATTERN);
		Matcher matcher = pattern.matcher(by + ",");
		log.debug("Pattern is {}",pattern);
		log.debug("Matcher is {}",matcher);
		List<SearchCriteria> params = new ArrayList<>();
		while (matcher.find()) {
			params.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
		}
		log.debug("List of search criteria: {}",params.size());
		return params;
	}

	public String getParam(List<SearchCriteria> params, String key) {
		for (SearchCriteria searchCriteria : params) {
			if (key.equalsIgnoreCase(searchCriteria.getKey())) {
				return searchCriteria.getValue().toString();
			}
		}
		return null;
	}
	
	/**
	 * @param by
	 * @param dateformat
	 * 	The date format : ms or some ISO like date string yyyyMMdd'T'HHmmssX.
	 * @return
	 */
	public List<SearchCriteria> createMatcherCriteria(String by, String dateformat) {
		DateTimeFormatter dtformatter = null;
		if (!dateformat.equals("ms")) {
			dtformatter = DateTimeFormatter.ofPattern(dateformat);
		}
		Pattern pattern = Pattern.compile(QRY_PATTERN);
		Matcher matcher = pattern.matcher(by + ",");
		log.debug("Pattern is {}",pattern);
		log.debug("Matcher is {}",matcher);
		List<SearchCriteria> params = new ArrayList<>();
		while (matcher.find()) {
			String varname = matcher.group(1).toLowerCase();
			String op = matcher.group(2);
			String val = matcher.group(3);
			val = val.replaceAll("\\*", "\\%");
			if (dtformatter != null && (varname.contains("time"))) {
				ZonedDateTime zdtInstanceAtOffset = ZonedDateTime.parse(val, dtformatter);
				ZonedDateTime zdtInstanceAtUTC = zdtInstanceAtOffset.withZoneSameInstant(ZoneOffset.UTC);
				Long tepoch = zdtInstanceAtUTC.toInstant().toEpochMilli();
				log.info("Parsed date at UTC : {}; use epoch {}",zdtInstanceAtUTC,tepoch);
				val = tepoch.toString();
			}
			params.add(new SearchCriteria(varname, op, val));
		}
		log.debug("List of search criteria: {}",params.size());
		return params;
	}

	/**
	 * @param key
	 * @param op
	 * @param val
	 * @return
	 */
	public List<SearchCriteria> createCriteria(String key, String op, String val) {

		List<SearchCriteria> params = new ArrayList<>();
		params.add(new SearchCriteria(key,op,val));
		return params;
	}

}
