package fi.vm.yti.common.service;

import fi.vm.yti.security.YtiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditService {

    private final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private final String entityType;

    public AuditService(String entity) {
        this.entityType = entity;
    }

    public void log(ActionType type, String uri, YtiUser user) {
        if (user.isAnonymous()) {
            return;
        }
        logger.info("{} {}: <{}>, User[id={}]", entityType, type, uri, user.getId());
    }

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE,
        SAVE,
    }
}
