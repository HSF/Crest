/**
 * 
 */
package hep.crest.server.controllers;

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

	private String QRY_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:|<|>)([a-zA-Z0-9_\\-\\/\\.]+?),";
	private String SORT_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:)([ASC|DESC]+?),";

	private static final Integer MAX_PAGE_SIZE = 10000;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PageRequestHelper() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PageRequest createPageRequest(Integer page, Integer size, String sort) {

		if (size > MAX_PAGE_SIZE) {
			log.warn("Requested size exceed maximum page size...change it to "+MAX_PAGE_SIZE);
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
			log.debug("Creating new order: " + direc + " " + field);
			orderlist.add(new Order(direc, field));
		}
		log.debug("Created list of sorting orders of size " + orderlist.size());
		Order orders[] = new Order[orderlist.size()];
		int i = 0;
		for (Order order : orderlist) {
			log.debug("Order @ " + i + " = " + order);
			orders[i++] = order;
		}
		Sort msort = new Sort(orders);
		PageRequest preq = new PageRequest(page, size, msort);

		return preq;
	}

	/**
	 * @param by
	 * @return
	 */
	public List<SearchCriteria> createMatcherCriteria(String by) {

		Pattern pattern = Pattern.compile(QRY_PATTERN);
		Matcher matcher = pattern.matcher(by + ",");
		log.debug("Pattern is " + pattern);
		log.debug("Matcher is " + matcher);
		List<SearchCriteria> params = new ArrayList<>();
		while (matcher.find()) {
			params.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
		}
		log.debug("List of search criteria: " + params.size());
		return params;
	}

}
