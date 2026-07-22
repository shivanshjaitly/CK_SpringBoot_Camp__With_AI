package in.codekerdos.booking.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceAuditAspect.class);

    @Pointcut("within(in.codekerdos.booking.service..*)")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable {
        String name = pjp.getSignature().toShortString();
        long start = System.currentTimeMillis();
        log.info("AUDIT start {}", name);
        try {
            Object result = pjp.proceed();
            log.info("AUDIT ok {} ({} ms)", name, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.warn("AUDIT fail {} ({} ms): {}", name, System.currentTimeMillis() - start, t.getMessage());
            throw t;
        }
    }
}
