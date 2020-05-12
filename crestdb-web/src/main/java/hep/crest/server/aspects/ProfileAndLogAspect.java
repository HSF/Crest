/**
 * 
 */
package hep.crest.server.aspects;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @version %I%, %G%
 * @author formica
 *
 */
@Aspect
@Component
public class ProfileAndLogAspect {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ProfileAndLogAspect.class);

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
        final Object[] args = joinPoint.getArgs();

        Arrays.stream(args).forEach(s -> log.debug("Profile method {} with argument: {}",
                joinPoint.toShortString(), s));
        final Object proceed = joinPoint.proceed();
        final long executionTime = System.currentTimeMillis() - start;
        log.info("{} executed in {} ms", joinPoint.getSignature(), executionTime);
        return proceed;
    }

}
