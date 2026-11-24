package academy.carol.portal.service;

import academy.carol.portal.domain.AuditLog;
import academy.carol.portal.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditLogService {
    private final AuditLogRepository logs;

    public AuditLogService(AuditLogRepository logs) {
        this.logs = logs;
    }

    public void record(String actor, String action, String section, String details) {
        logs.save(new AuditLog(limit(actor, 100), limit(action, 80), limit(section, 120),
                limit(details, 2000), clientAddress()));
    }

    private String clientAddress() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return limit(((ServletRequestAttributes) attributes).getRequest().getRemoteAddr(), 64);
        }
        return "";
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}
