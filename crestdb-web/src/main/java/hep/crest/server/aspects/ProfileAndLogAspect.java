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

//import nats.client.Nats;

/**
 * @author formica
 *
 */
@Aspect
@Component
public class ProfileAndLogAspect {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Around("@annotation(hep.crest.server.annotations.ProfileAndLog)")
	public Object profileAndLog(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		Object[] args = joinPoint.getArgs();
		
		Arrays.stream(args).forEach(s -> log.debug("Profile method "+joinPoint.toShortString()+" with arguments "+s));
	    Object proceed = joinPoint.proceed();
	    long executionTime = System.currentTimeMillis() - start;
	    log.debug(joinPoint.getSignature() + " executed in " + executionTime + "ms");
	    return proceed;
	}
	
}
