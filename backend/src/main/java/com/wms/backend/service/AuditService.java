package com.wms.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.backend.entity.AuditLog;
import com.wms.backend.repository.AuditLogRepository;
import com.wms.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper       objectMapper;

    // Log any data change to the audit log
    // action: CREATE, UPDATE, DELETE, STATUS_CHANGE
    // entityType: Order, Invoice, Product, Customer etc.
    // entityId: the UUID of the entity that changed
    // oldValue: the state before the change (null for creates)
    // newValue: the state after the change (null for deletes)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action,
                    String entityType,
                    UUID entityId,
                    Object oldValue,
                    Object newValue) {

        // Propagation.REQUIRES_NEW means this runs in its own transaction
        // Even if the outer transaction rolls back, the audit log is saved
        // This is important in order to want to know that something was attempted
        // even if it failed

        UUID userId     = null;
        UUID businessId = null;

        // Safely get user context
        // audit logging should never crash
        // even if called outside a security context
        try {
            userId     = SecurityUtils.getCurrentUserId();
            businessId = SecurityUtils.getCurrentBusinessId();
        } catch (Exception e) {
            log.debug("Audit log called without security context");
        }

        AuditLog entry = new AuditLog();
        entry.setBusinessId(businessId);
        entry.setUserId(userId);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setOldValue(toJson(oldValue));
        entry.setNewValue(toJson(newValue));
        entry.setOccurredAt(Instant.now());

        auditLogRepository.save(entry);
    }

    // method for status changes
    // Status changes are most common audit event
    public void logStatusChange(String entityType,
                                UUID entityId,
                                String fromStatus,
                                String toStatus) {
        log(
                "STATUS_CHANGE",
                entityType,
                entityId,
                fromStatus != null
                        ? java.util.Map.of("status", fromStatus)
                        : null,
                java.util.Map.of("status", toStatus)
        );
    }

    // Convert any object to a JSON string for storage
    // Returns null if the object is null or serialisation fails
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialise audit value: {}", e.getMessage());
            return obj.toString();
        }
    }
}