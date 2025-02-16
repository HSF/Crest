package hep.crest.server;

import hep.crest.server.converters.FolderMapper;
import hep.crest.server.converters.GenericMapper;
import hep.crest.server.converters.GlobalTagMapMapper;
import hep.crest.server.converters.GlobalTagMapper;
import hep.crest.server.converters.IovMapper;
import hep.crest.server.converters.RunLumiMapper;
import hep.crest.server.converters.TagMapper;
import hep.crest.server.converters.TagMetaMapper;
import hep.crest.server.data.pojo.CrestFolders;
import hep.crest.server.data.pojo.GlobalTag;
import hep.crest.server.data.pojo.GlobalTagMap;
import hep.crest.server.data.pojo.GlobalTagMapId;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.IovId;
import hep.crest.server.data.pojo.PayloadInfoData;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.pojo.TagMeta;
import hep.crest.server.data.runinfo.pojo.RunLumiId;
import hep.crest.server.data.runinfo.pojo.RunLumiInfo;
import hep.crest.server.repositories.monitoring.PayloadInfoMapper;
import hep.crest.server.swagger.model.FolderDto;
import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.GlobalTagMapDto;
import hep.crest.server.swagger.model.IovDto;
import hep.crest.server.swagger.model.PayloadTagInfoDto;
import hep.crest.server.swagger.model.RunLumiInfoDto;
import hep.crest.server.swagger.model.TagDto;
import hep.crest.server.swagger.model.TagMetaDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class MapperTest {

    @Autowired
    private ApplicationContext applicationContext;

    private final Random rnd = new Random();

    public void fillRandom(Object obj, Class<?> clazz) {
        try {
            Method[] publicMethods = clazz.getMethods();
            for (Method aMethod : publicMethods) {
                if (aMethod.getName().startsWith("set")
                        && aMethod.getParameterCount() == 1) {
                    Class<?> argtype = aMethod.getParameterTypes()[0];
                    if (argtype.equals(Double.class)) {
                        Double val = rnd.nextDouble();
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(Float.class)) {
                        Float val = rnd.nextFloat();
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(BigDecimal.class)) {
                        aMethod.invoke(obj, BigDecimal.valueOf(rnd.nextLong()));
                    } else if (argtype.equals(BigInteger.class)) {
                        aMethod.invoke(obj, BigInteger.valueOf(rnd.nextLong()));
                    } else if (argtype.equals(Long.class)) {
                        Long val = rnd.nextLong();
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(Integer.class)) {
                        Integer val = rnd.nextInt();
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(String.class)) {
                        String val = String.valueOf(rnd.nextInt()); // TODO generate better string
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(Date.class)) {
                        Instant now = Instant.now();
                        Date val = Date.from(Instant.ofEpochMilli(now.toEpochMilli()));
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(Timestamp.class)) {
                        Instant now = Instant.now();
                        Timestamp val = Timestamp.from(Instant.ofEpochMilli(now.toEpochMilli()));
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(OffsetDateTime.class)) {
                        Instant now = Instant.now();
                        OffsetDateTime val = Instant.ofEpochMilli(now.toEpochMilli()).atOffset(ZoneOffset.UTC);
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(Boolean.class)) {
                        Boolean val = rnd.nextBoolean();
                        aMethod.invoke(obj, val);
                    } else {
                        log.warn("fillRandom: not calling setter method {}", aMethod);
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InternalError(e);
        }
    }

    public <D, T> void testMapper(Class<?> pojoType, Class<?> dtoType, Class<?
            extends GenericMapper<D, T>> mapperClass) {
        try {
            GenericMapper<D, T> mapper = applicationContext.getBean(mapperClass);
            Object item = pojoType.getDeclaredConstructor().newInstance();
            fillRandom(item, pojoType);
            log.info("Generated item    = {}", item);
            Object dto = mapper.toDto((T) item);
            log.info("Converted to dto  = {}", dto);
            Object pojo = mapper.toEntity((D) dto);
            log.info("Converted to pojo = {}", pojo);
            assertEquals(pojo, item);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    public  <D, T> void compare(Object item, Class<?> dtoType, Class<?
            extends GenericMapper<D, T>> mapperClass) {
        try {
            GenericMapper<D, T> mapper = applicationContext.getBean(mapperClass);

            log.info("Generated item    = {}", item);
            Object dto = mapper.toDto((T) item);
            log.info("Converted to dto  = {}", dto);
            Object pojo = mapper.toEntity((D) dto);
            log.info("Converted to pojo = {}", pojo);
            assertEquals(pojo, item);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    @Test
    public void testGlobalTags() {
        log.info("Test Global Tag conversion");
        testMapper(GlobalTag.class, GlobalTagDto.class, GlobalTagMapper.class);
    }

    @Test
    public void testTags() {
        log.info("Test Tag conversion");
        testMapper(Tag.class, TagDto.class, TagMapper.class);
    }

    @Test
    public void testIovs() {
        log.info("Test Iov conversion");
        IovId id = new IovId();
        fillRandom(id, IovId.class);
        Iov iov = new Iov();
        fillRandom(iov, Iov.class);
        iov.setId(id);
        compare(iov, IovDto.class, IovMapper.class);
    }


    @Test
    public void testRuns() {
        log.info("Test Run conversion");
        RunLumiId id = new RunLumiId();
        fillRandom(id, RunLumiId.class);
        RunLumiInfo li = new RunLumiInfo();
        fillRandom(li, RunLumiInfo.class);
        li.setId(id);
        compare(li, RunLumiInfoDto.class, RunLumiMapper.class);
    }

    @Test
    public void testGlobalTagMaps() {
        log.info("Test Global Tag Map conversion");
        GlobalTagMapId id = new GlobalTagMapId();
        fillRandom(id, GlobalTagMapId.class);
        Tag tag = new Tag();
        fillRandom(tag, Tag.class);
        GlobalTagMap map = new GlobalTagMap();
        fillRandom(map, GlobalTagMap.class);
        map.setTag(tag).setId(id);
        compare(map, GlobalTagMapDto.class, GlobalTagMapMapper.class);
    }

    @Test
    public void testCrestFolders() {
        log.info("Test Folder conversion");
        testMapper(CrestFolders.class, FolderDto.class, FolderMapper.class);
    }


    @Test
    public void testTagMeta() {
        log.info("Test Tag Meta conversion");
        testMapper(TagMeta.class, TagMetaDto.class, TagMetaMapper.class);
    }

}
