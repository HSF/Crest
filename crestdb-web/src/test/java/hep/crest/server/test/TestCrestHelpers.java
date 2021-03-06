package hep.crest.server.test;

import com.querydsl.core.types.dsl.BooleanExpression;
import hep.crest.data.repositories.querydsl.GlobalTagFiltering;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class TestCrestHelpers {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testA_PageRequest() throws Exception {
        PageRequestHelper prh = new PageRequestHelper();
        PageRequest preq = prh.createPageRequest(0,100000,"name:DESC");
        assertThat(preq).isNotNull();

        String by="name:TEST,insertiontime>0";
        List<SearchCriteria> clist = prh.createMatcherCriteria(by,"ms");
        assertThat(clist.size()).isPositive();

        String cond = prh.getParam(clist,"name");
        assertThat(cond.length()).isPositive();

        IFilteringCriteria filtering = new GlobalTagFiltering();
        BooleanExpression exp = prh.buildWhere(filtering, clist);
    }

    @Test
    public void testB_ExceptionTest() {
        final NullPointerException np = new NullPointerException("null");
        final AlreadyExistsPojoException es = new AlreadyExistsPojoException("message");
        assertThat(es.getMessage()).contains("message");
        final AlreadyExistsPojoException ees = new AlreadyExistsPojoException("message", np);
        assertThat(ees.getCause()).isNotNull();
        final NotExistsPojoException e = new NotExistsPojoException("message");
        assertThat(e.getMessage()).contains("message");
        final NotExistsPojoException ee = new NotExistsPojoException("message", np);
        assertThat(ee.getCause()).isNotNull();
    }

}
