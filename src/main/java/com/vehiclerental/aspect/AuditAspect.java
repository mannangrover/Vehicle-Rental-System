package com.vehiclerental.aspect;

import com.vehiclerental.dao.AuditLogDao;
import com.vehiclerental.model.SessionUser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private static final int MAX_DETAILS_LENGTH = 255;

    private final AuditLogDao auditLogDao;

    public AuditAspect(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    @AfterReturning("@annotation(audited)")
    public void writeAuditLog(JoinPoint joinPoint, Audited audited) {
        try {
            auditLogDao.insert(audited.value(), describe(joinPoint), currentActor());
        } catch (DataAccessException e) {
            // The action itself succeeded, so a logging problem must not show the user an error.
            log.error("Could not write audit log for {}", audited.value(), e);
        }
    }

    /** Only simple values are included, so passwords and form contents can never reach the log. */
    private String describe(JoinPoint joinPoint) {
        String args = Arrays.stream(joinPoint.getArgs())
                .filter(this::isSafeToLog)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        String details = joinPoint.getSignature().getName() + "(" + args + ")";
        return details.length() > MAX_DETAILS_LENGTH ? details.substring(0, MAX_DETAILS_LENGTH) : details;
    }

    private boolean isSafeToLog(Object arg) {
        return arg instanceof Long || arg instanceof LocalDateTime || arg instanceof Enum<?> || arg instanceof BigDecimal;
    }

    private String currentActor() {
        RequestAttributes request = RequestContextHolder.getRequestAttributes();
        if (request != null
                && request.getAttribute(SessionUser.SESSION_KEY, RequestAttributes.SCOPE_SESSION) instanceof SessionUser user) {
            return user.getEmail();
        }
        return "system";
    }
}
