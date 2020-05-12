/**
 * 
 */
package hep.crest.server.controllers;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.swagger.model.GenericMap;

/**
 * PageRequestHelper is a utility class to treat input parameter from HTTP
 * requests. It generated then list of criterias and ordering statement. Those
 * will be used in SQL requests.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
@Component
public class PageRequestHelper {

    /**
     * The query pattern.
     */
    private static final String QRY_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:|<|>)([a-zA-Z0-9_\\-\\/\\.]+?),";
    /**
     * The sort pattern.
     */
    private static final String SORT_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:)([ASC|DESC]+?),";

    /**
     * Maximum page size.
     */
    private static final Integer MAX_PAGE_SIZE = 10000;
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PageRequestHelper.class);

    /**
     * Default ctor.
     */
    public PageRequestHelper() {
        super();
    }

    /**
     * @param page
     *            the Integer
     * @param size
     *            the Integer
     * @param sort
     *            the Integer
     * @return PageRequest
     */
    public PageRequest createPageRequest(Integer page, Integer size, String sort) {

        if (size > MAX_PAGE_SIZE) {
            log.warn("Requested size exceed maximum page size...change it to {}", MAX_PAGE_SIZE);
            size = MAX_PAGE_SIZE;
        }
        final Pattern sortpattern = Pattern.compile(SORT_PATTERN);
        final Matcher sortmatcher = sortpattern.matcher(sort + ",");
        final List<Order> orderlist = new ArrayList<>();
        while (sortmatcher.find()) {
            Direction direc = Direction.ASC;
            if ("DESC".equals(sortmatcher.group(3))) {
                direc = Direction.DESC;
            }
            final String field = sortmatcher.group(1);
            log.debug("Creating new order: {} {}", direc, field);
            orderlist.add(new Order(direc, field));
        }
        log.debug("Created list of sorting orders of size {}", orderlist.size());
        final Order[] orders = new Order[orderlist.size()];
        int i = 0;
        for (final Order order : orderlist) {
            log.debug("Order @ {} = {} ", i, order);
            orders[i] = order;
            i++;
        }
        final Sort msort = Sort.by(orders);
        return PageRequest.of(page, size, msort);
    }

    /**
     * @param by
     *            the String
     * @return List<SearchCriteria>
     */
    public List<SearchCriteria> createMatcherCriteria(String by) {

        final Pattern pattern = Pattern.compile(QRY_PATTERN);
        final Matcher matcher = pattern.matcher(by + ",");
        log.debug("Pattern is {}", pattern);
        log.debug("Matcher is {}", matcher);
        final List<SearchCriteria> params = new ArrayList<>();
        while (matcher.find()) {
            params.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
        }
        log.debug("List of search criteria: {}", params.size());
        return params;
    }

    /**
     * @param params
     *            the List<SearchCriteria>
     * @param key
     *            the String
     * @return String
     */
    public String getParam(List<SearchCriteria> params, String key) {
        for (final SearchCriteria searchCriteria : params) {
            if (key.equalsIgnoreCase(searchCriteria.getKey())) {
                return searchCriteria.getValue().toString();
            }
        }
        return null;
    }

    /**
     * @param by
     *            the String
     * @param dateformat
     *            The date format : ms or some ISO like date string
     *            yyyyMMdd'T'HHmmssX.
     * @return List<SearchCriteria>
     */
    public List<SearchCriteria> createMatcherCriteria(String by, String dateformat) {
        DateTimeFormatter dtformatter = null;
        if (!"ms".equals(dateformat)) {
            dtformatter = DateTimeFormatter.ofPattern(dateformat);
        }
        log.debug("Date format used is {}", dateformat);
        final Pattern pattern = Pattern.compile(QRY_PATTERN);
        final Matcher matcher = pattern.matcher(by + ",");
        log.debug("Pattern is {}", pattern);
        log.debug("Matcher is {}", matcher);
        final List<SearchCriteria> params = new ArrayList<>();
        while (matcher.find()) {
            final String varname = matcher.group(1).toLowerCase(Locale.ENGLISH);
            final String op = matcher.group(2);
            String val = matcher.group(3);
            val = val.replaceAll("\\*", "\\%");
            if (dtformatter != null && varname.contains("time")) {
                final ZonedDateTime zdtInstanceAtOffset = ZonedDateTime.parse(val, dtformatter);
                final ZonedDateTime zdtInstanceAtUTC = zdtInstanceAtOffset
                        .withZoneSameInstant(ZoneOffset.UTC);
                final Long tepoch = zdtInstanceAtUTC.toInstant().toEpochMilli();
                log.info("Parsed date at UTC : {}; use epoch {}", zdtInstanceAtUTC, tepoch);
                val = tepoch.toString();
            }
            params.add(new SearchCriteria(varname, op, val));
        }
        log.debug("List of search criteria: {}", params.size());
        return params;
    }

    /**
     * @param key
     *            the String
     * @param op
     *            the String
     * @param val
     *            the String
     * @return List<SearchCriteria>
     */
    public List<SearchCriteria> createCriteria(String key, String op, String val) {

        final List<SearchCriteria> params = new ArrayList<>();
        params.add(new SearchCriteria(key, op, val));
        return params;
    }

    /**
     * @param expressions
     *            the List<BooleanExpression>
     * @return BooleanExpression
     */
    public BooleanExpression getWhere(List<BooleanExpression> expressions) {
        BooleanExpression wherepred = null;

        for (final BooleanExpression exp : expressions) {
            if (wherepred == null) {
                wherepred = exp;
            }
            else {
                wherepred = wherepred.and(exp);
            }
        }
        return wherepred;
    }

    /**
     * @param params
     *            the List<SearchCriteria>
     * @return GenericMap
     */
    public GenericMap getFilters(List<SearchCriteria> params) {
        final GenericMap filters = new GenericMap();
        for (final SearchCriteria sc : params) {
            filters.put(sc.getKey(), sc.getValue().toString());
        }
        return filters;
    }

    /**
     * @param filter
     *            the IFilteringCriteria
     * @param by
     *            the String
     * @return BooleanExpression
     */
    public BooleanExpression buildWhere(IFilteringCriteria filter, String by) {
        final List<SearchCriteria> params = createMatcherCriteria(by);
        final List<BooleanExpression> expressions = filter.createFilteringConditions(params);
        return getWhere(expressions);
    }

    /**
     * @param filter
     *            the IFilteringCriteria
     * @param params
     *            the List<SearchCriteria>
     * @return BooleanExpression
     */
    public BooleanExpression buildWhere(IFilteringCriteria filter, List<SearchCriteria> params) {
        final List<BooleanExpression> expressions = filter.createFilteringConditions(params);
        return getWhere(expressions);
    }
}
