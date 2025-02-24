package hep.crest.server.utils;

import hep.crest.server.data.pojo.GlobalTag;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

@Slf4j
public class RandomGenerator {

    private final Random rnd = new Random();

    public boolean isAttributeSetter(Field[] fields, String methname) {
        if (methname.startsWith("set")) {
            return Boolean.TRUE;
        }
        return Arrays.stream(fields).anyMatch(
                a -> methname.toLowerCase(Locale.ROOT).contains(a.getName().toLowerCase(Locale.ROOT)));
    }

    public void fillRandom(Object obj, Class<?> clazz) {
        try {
            log.info("Filling object {} of class {} with random data", obj, clazz);
            Method[] publicMethods = clazz.getMethods();
            Field[] fields = clazz.getDeclaredFields();
            for (Method aMethod : publicMethods) {
                if (isAttributeSetter(fields, aMethod.getName())
                    && aMethod.getParameterCount() == 1) {
                    log.info("Call setter for {} with param {}", aMethod.getName(), aMethod.getParameterTypes()[0]);
                    Class<?> argtype = aMethod.getParameterTypes()[0];
                    if (argtype.equals(Double.class)) {
                        Double val = rnd.nextDouble();
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(Float.class)) {
                        Float val = rnd.nextFloat();
                        aMethod.invoke(obj, val);
                    } else if (argtype.equals(BigDecimal.class)) {
                        aMethod.invoke(obj, BigDecimal.valueOf(rnd.nextDouble()));
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
                        log.warn("fillRandom: not calling setter on method {}", aMethod);
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Cannot fill object: {}", e.getMessage());
            e.printStackTrace();
            throw new InternalError(e);
        }
    }

    public Object generate(Class<?> pojoType) {
        try {
            log.info("Use ctors: {}", pojoType.getDeclaredConstructor());
            Object item = pojoType.getDeclaredConstructor().newInstance();
            log.info("new instance created: {}", (item).toString());
            fillRandom(item, pojoType);
            log.info("Generate item : {} of class type {}", item, pojoType);
            return item;
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    public static void main(String[] args) {

        RandomGenerator rndgen = new RandomGenerator();
        GlobalTag gt = new GlobalTag();

        Field[] farr = gt.getClass().getDeclaredFields();
        for (Field f : farr) {
            System.out.println("Field " + f.getName());
        }
        Method[] publicMethods = gt.getClass().getMethods();
        for (Method m : publicMethods) {
            System.out.println("Analyse Method: " + m.getReturnType() + " " + m.getName()
                               + "[" + m.getParameterCount() + "]");
            Boolean issetter = rndgen.isAttributeSetter(farr, m.getName());
            System.out.println("Method " + m.getName() + " has setter status " + issetter);
        }

        GlobalTag gtgen = (GlobalTag) rndgen.generate(GlobalTag.class);
        System.out.println("Generated class is : " + gtgen.getClass());
    }
}
