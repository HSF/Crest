/**
 *
 */
package hep.crest.server.controllers;

import hep.crest.server.config.CrestProperties;
import hep.crest.server.exceptions.CdbBadRequestException;
import hep.crest.server.serializers.ArgTimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@Slf4j
public class PageRequestHelper {

    /**
     * The sort pattern.
     */
    private static final String SORT_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:)(ASC|DESC),";

    /**
     * Maximum page size.
     */
    private static final Integer MAX_PAGE_SIZE = 10000;
    /**
     * ISO pattern in input.
     */
    private static final String ISO_PATTERN = "yyyyMMdd'T'HHmmssz";

    /**
     * Cool Max data in COOL format.
     */
    public static final long COOL_MAX_DATE = 9223372036854775807L;
    /**
     * Cool Max run in COOL format.
     */
    public static final long COOL_MAX_RUN = 2147483647L;
    /**
     * Cool Max lumi block in COOL format.
     */
    public static final long COOL_MAX_LUMIBLOCK = 4294967295L;
    /**
     *
     */
    private static final int COOLIOV_RUN_MASK = 32;
    /**
     *
     */
    public static final BigInteger LUMIMASK = new BigInteger("00000000FFFFFFFF", 16);
    /**
     *
     */
    public static final BigDecimal TO_NANO_SECONDS = new BigDecimal(1000000L);

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
     * Return time in ArgTimeUnit since epoch, using the format provided in dateformat.
     * If the dateformat is msec or sec then it will just interpret the string as a long.
     * The value will then depend on the provided input argument.
     *
     * @param val the time string.
     * @param inputformat the time format (sec, msec, iso, run).
     * @param outunit the time unit (sec or msec).
     * @param customdateformat a user defined date format.
     * @return BigInteger
     */
    public BigInteger getTimeFromArg(String val, ArgTimeUnit inputformat, ArgTimeUnit outunit,
                                     String customdateformat) {
        try {
            log.debug("Get time from args: {} {} {} {}", val, inputformat, outunit, customdateformat);
            DateTimeFormatter dtformatter = null;
            BigInteger tepoch = null;
            if (val == null) {
                return tepoch;
            }
            if (inputformat == null) {
                inputformat = ArgTimeUnit.MS;
            }
            if (val.equalsIgnoreCase("INF")) {
                // Until time is INF.
                log.warn("The time will be set to INF : {}", CrestProperties.INFINITY);
                return CrestProperties.INFINITY.toBigInteger();
            }
            boolean iscoolformat = Boolean.FALSE;
            log.trace("Getting time from arg {}, {}, {}, {}", val, inputformat, outunit, customdateformat);
            switch (inputformat) {
                // Tepoch will always be millisec since 1970 at the end of this block.
                case MS:
                    log.trace("Use MS to parse {}", val);
                    tepoch = BigInteger.valueOf(Long.parseLong(val));
                    break;
                case SEC:
                    log.trace("Use SEC to parse {} (*1000 to get epoch)", val);
                    tepoch = BigInteger.valueOf(Long.parseLong(val) * 1000L);
                    break;
                case RUN:
                    log.trace("Use run...{}", val);
                    tepoch = BigInteger.valueOf(Long.parseLong(val));
                    tepoch = tepoch.shiftLeft(COOLIOV_RUN_MASK);
                    iscoolformat = Boolean.TRUE;
                    break;
                case RUN_LUMI:
                    log.trace("Use run-lumi...{}", val);
                    String[] rl = val.split("-");
                    tepoch = getCoolRunLumi(rl[0], rl[1]);
                    iscoolformat = Boolean.TRUE;
                    break;
                case ISO:
                    log.trace("Use ISO pattern {} to parse {}", ISO_PATTERN, val);
                    dtformatter = DateTimeFormatter.ofPattern(ISO_PATTERN);
                    ZonedDateTime zdtInstanceAtOffset = ZonedDateTime.parse(val, dtformatter);
                    ZonedDateTime zdtInstanceAtUTC = zdtInstanceAtOffset
                            .withZoneSameInstant(ZoneOffset.UTC);
                    tepoch = BigInteger.valueOf(zdtInstanceAtUTC.toInstant().toEpochMilli());
                    log.trace("Parsed date using -iso- format {}; time from epoch is {}", zdtInstanceAtUTC, tepoch);
                    break;
                case CUSTOM:
                    log.debug("Use CUSTOM pattern {} to parse {}", customdateformat, val);
                    if (customdateformat == null) {
                        customdateformat = ISO_PATTERN;
                    }
                    dtformatter = DateTimeFormatter.ofPattern(customdateformat);
                    ZonedDateTime customzdtInstanceAtOffset = ZonedDateTime.parse(val, dtformatter);
                    ZonedDateTime customzdtInstanceAtUTC = customzdtInstanceAtOffset
                            .withZoneSameInstant(ZoneOffset.UTC);
                    tepoch = BigInteger.valueOf(customzdtInstanceAtUTC.toInstant().toEpochMilli());
                    log.trace("Parsed date using custom format {} - {}; time from epoch is {}", customdateformat,
                            customzdtInstanceAtUTC, tepoch);
                    break;
                case NUMBER:
                    log.debug("Use number to parse {}", val);
                    tepoch = new BigInteger(val);
                    iscoolformat = Boolean.TRUE;
                    break;
                default:
                    // cannot arrive here.
                    log.error("Cannot process argument time parsing");
                    break;
            }
            // Assume we return milli seconds.
            log.trace("Time arg parsing will return {} ", tepoch);
            if (tepoch == null) {
                return tepoch;
            }
            if (outunit != null && outunit.equals(ArgTimeUnit.SEC)) {
                // Here we return seconds since Epoch.
                return tepoch.divide(BigInteger.valueOf(1000L));
            }
            else if (outunit != null && outunit.equals(ArgTimeUnit.COOL) && !iscoolformat) {
                return tepoch.multiply(TO_NANO_SECONDS.toBigInteger());
            }
            return tepoch;
        }
        catch (RuntimeException e) {
            throw new CdbBadRequestException("Error while parsing time", e);
        }
    }

    /**
     * @param arun
     *            The run number as a String.
     * @param lb
     *            The lumi block as a String.
     * @return The COOL time.
     */
    public BigInteger getCoolRunLumi(final String arun, final String lb) {
        Long runlong = null;
        Long lblong = null;
        if (arun == null) {
            return null;
        }
        if (arun.equalsIgnoreCase("INF")) {
            runlong = COOL_MAX_RUN;
            lblong = COOL_MAX_LUMIBLOCK;
        }
        else {
            if (lb.equals("MAXLB")) {
                lblong = COOL_MAX_LUMIBLOCK;
            }
            else {
                lblong = Long.parseLong(lb);
            }
            runlong = Long.parseLong(arun);
        }
        return getCoolRunLumi(runlong, lblong);
    }

    /**
     * @param arun
     *            The run in long.
     * @param lb
     *            The lb in long.
     * @return The COOL time.
     */
    public BigInteger getCoolRunLumi(final Long arun, final Long lb) {
        BigInteger irun = null;
        BigInteger ilb = null;
        BigInteger runlumi = null;
        BigInteger run = null;
        if (arun == null) {
            return null;
        }
        else {
            irun = BigInteger.valueOf(arun);
            if (lb == null) {
                ilb = BigInteger.valueOf(0L);
            }
            else {
                ilb = BigInteger.valueOf(lb);
            }
            run = irun.shiftLeft(COOLIOV_RUN_MASK);
            runlumi = run.or(ilb);
        }
        return runlumi;
    }
}
