package academy.carol.portal.config;

import academy.carol.portal.service.AuditLogService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationAuditListener {
    private final AuditLogService auditLogs;

    public AuthenticationAuditListener(AuditLogService auditLogs) {
        this.auditLogs = auditLogs;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        auditLogs.record(event.getAuthentication().getName(), "LOGIN_SUCCESS",
                "Authentication", "Successful sign-in.");
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        auditLogs.record(event.getAuthentication().getName(), "LOGIN_FAILED",
                "Authentication", "Sign-in rejected.");
    }

}
