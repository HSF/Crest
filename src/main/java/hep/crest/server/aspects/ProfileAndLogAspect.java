/**
 * 
 */
package hep.crest.server.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @version %I%, %G%
 * @author formica
 *
 */
@Aspect
@Component
@Slf4j
public class ProfileAndLogAspect {

    /**
     * The jackson mapper.
     */
    private final ObjectMapper mapper;

    /**
     * Default constructor, using injection.
     * @param mapper
     */
    @Autowired
    public ProfileAndLogAspect(@Qualifier("jacksonMapper") ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param joinPoint
     *            the ProceedingJoinPoint
     * @return Object
     * @throws Throwable
     *             If an Exception occurred
     */
    @Around("@annotation(hep.crest.server.annotations.ProfileAndLog)")
    public Object profileAndLog(ProceedingJoinPoint joinPoint) throws Throwable {
        final long start = System.currentTimeMillis();
        log.debug("Start profiling at time: {}", start);
        log.debug("Profile method {} ", joinPoint.getSignature().getName());
        final Object[] args = joinPoint.getArgs();

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        String[] parameters = codeSignature.getParameterNames();
        Class[] paramTypes = codeSignature.getParameterTypes();
        log.debug("Parameters {} and types {}", parameters, paramTypes);

        Map<String, Object> esParams = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equals("securityContext")
                || parameters[i].equals("info")
                || parameters[i].equals("filesBodypart")
                || paramTypes[i].getName().contains("List")) {
                continue;
            }
            if (args[i] == null) {
                continue;
            }
            log.debug("Set parameter {} of type {} with value {}", parameters[i], paramTypes[i], args[i]);
            esParams.put(parameters[i], args[i]);
        }
        final Object proceed = joinPoint.proceed();
        final long executionTime = System.currentTimeMillis() - start;
        // Set MDC context data
        MDC.put("crest_execution_time", String.valueOf(executionTime));
        MDC.put("token", "crest");
        if (esParams.containsKey("hash")) {
            MDC.put("hash", esParams.get("hash").toString());
        }
        if (esParams.containsKey("tagname")) {
            MDC.put("name", esParams.get("tagname").toString());
        }
        if (esParams.containsKey("name")) {
            MDC.put("name", esParams.get("name").toString());
        }
        MDC.put("crest_method", joinPoint.getSignature().getName());
        esParams.put("crest_profile", joinPoint.toShortString());
        log.info("{}", esParams);
        MDC.clear();
        return proceed;
    }

}
