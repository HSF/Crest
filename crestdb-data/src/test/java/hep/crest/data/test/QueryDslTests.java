package hep.crest.data.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.GlobalTagMapId;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.GlobalTagMapRepository;
import hep.crest.data.repositories.GlobalTagRepository;
import hep.crest.data.repositories.TagRepository;
import hep.crest.data.repositories.querydsl.GlobalTagFiltering;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.data.repositories.querydsl.TagFiltering;
import hep.crest.data.test.tools.DataGenerator;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryDslTests {

 
    private static final String SORT_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:)([ASC|DESC]+?),";
    private static final String QRY_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:|<|>)([a-zA-Z0-9_\\-\\/\\.\\*\\%]+?),";

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private GlobalTagRepository globaltagrepository;

    @Autowired
    private TagRepository tagrepository;

    @Autowired
    private GlobalTagMapRepository tagmaprepository;


    @Test
    public void testGlobalTags() throws Exception {
        final GlobalTag gtag = DataGenerator.generateGlobalTag("MY-TEST-GT-01");
        globaltagrepository.save(gtag);
        final IFilteringCriteria filter = new GlobalTagFiltering();
        final PageRequest preq = createPageRequest(0, 10, "name:ASC");

        final List<SearchCriteria> params = createMatcherCriteria("name:M,workflow:%,scenario:%,release:rel,insertionTime>0");
        final List<BooleanExpression> expressions = filter.createFilteringConditions(params);
        BooleanExpression wherepred = null;

        for (final BooleanExpression exp : expressions) {
            if (wherepred == null) {
                wherepred = exp;
            }
            else {
                wherepred = wherepred.and(exp);
            }
        }
        final Page<GlobalTag> dtolist = globaltagrepository.findAll(wherepred, preq);
        assertThat(dtolist.getSize()).isGreaterThan(0);
    }
    
    @Test
    public void testTags() throws Exception {
        final Tag tag = DataGenerator.generateTag("MY-TEST-01","time");
        tagrepository.save(tag);
        final IFilteringCriteria filter = new TagFiltering();
        final PageRequest preq = createPageRequest(0, 10, "name:ASC");

        final List<SearchCriteria> params = createMatcherCriteria("name:M,timetype:time,objecttype:%,insertiontime>0");
        final List<BooleanExpression> expressions = filter.createFilteringConditions(params);
        BooleanExpression wherepred = null;

        for (final BooleanExpression exp : expressions) {
            if (wherepred == null) {
                wherepred = exp;
            }
            else {
                wherepred = wherepred.and(exp);
            }
        }
        final Page<Tag> dtolist = tagrepository.findAll(wherepred, preq);
        assertThat(dtolist.getSize()).isGreaterThan(0);
    }

    
    @Test
    public void testMappingTags() throws Exception {
        final GlobalTag gtag = DataGenerator.generateGlobalTag("MY-TEST-GT-02");
        globaltagrepository.save(gtag);
        final Tag tag1 = DataGenerator.generateTag("MY-TEST-02","time");
        final Tag tag2 = DataGenerator.generateTag("MY-SECOND-03","time");
        tagrepository.save(tag1);
        tagrepository.save(tag2);
        
        final GlobalTagMapId id1 = new GlobalTagMapId();
        id1.setGlobalTagName(gtag.getName());
        id1.setLabel("MY-TEST");
        id1.setRecord("aaa");
        final GlobalTagMap map1 = DataGenerator.generateMapping(gtag, tag1, id1);
        
        final GlobalTagMapId id2 = new GlobalTagMapId();
        id2.setGlobalTagName(gtag.getName());
        id2.setLabel("MY-SECOND");
        id2.setRecord("bbb");
        final GlobalTagMap map2 = DataGenerator.generateMapping(gtag, tag2, id2);

        tagmaprepository.save(map1);
        tagmaprepository.save(map2);
        
        final List<GlobalTagMap> gmlist = tagmaprepository.findByGlobalTagName(gtag.getName());
        assertThat(gmlist.size()).isGreaterThan(0);

        final List<GlobalTagMap> gmlistbytag = tagmaprepository.findByTagName(tag1.getName());
        assertThat(gmlistbytag.size()).isGreaterThan(0);

    }

    protected PageRequest createPageRequest(Integer page, Integer size, String sort) {

        final Pattern sortpattern = Pattern.compile(SORT_PATTERN);
        final Matcher sortmatcher = sortpattern.matcher(sort + ",");
        final List<Order> orderlist = new ArrayList<>();
        while (sortmatcher.find()) {
            Direction direc = Direction.ASC;
            if (sortmatcher.group(3).equals("DESC")) {
                direc = Direction.DESC;
            }
            final String field = sortmatcher.group(1);
            log.debug("Creating new order: {} {} ", direc, field);
            orderlist.add(new Order(direc, field));
        }
        log.debug("Created list of sorting orders of size {}", orderlist.size());
        final Order[] orders = new Order[orderlist.size()];
        int i = 0;
        for (final Order order : orderlist) {
            log.debug("Order @ {} = {}", i, order);
            orders[i++] = order;
        }
        final Sort msort = Sort.by(orders);
        return PageRequest.of(page, size, msort);
    }

    protected List<SearchCriteria> createMatcherCriteria(String by) {

        final Pattern pattern = Pattern.compile(QRY_PATTERN);
        final Matcher matcher = pattern.matcher(by + ",");
        log.debug("Pattern is {}", pattern);
        log.debug("Matcher is {}", matcher);
        final List<SearchCriteria> params = new ArrayList<>();
        while (matcher.find()) {
            String val = matcher.group(3);
            val = val.replaceAll("\\*", "\\%");
            params.add(new SearchCriteria(matcher.group(1), matcher.group(2), val));
        }
        log.debug("List of search criteria: {}", params.size());
        return params;
    }

}
