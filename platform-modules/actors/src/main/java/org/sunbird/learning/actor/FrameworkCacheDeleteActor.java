package org.sunbird.learning.actor;

import akka.actor.ActorRef;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.util.FrameworkCache;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.learning.common.enums.LearningErrorCodes;
import org.sunbird.learning.framework.FrameworkHierarchyOperations;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.concurrent.CompletableFuture;

public class FrameworkCacheDeleteActor extends BaseGraphManager {
    @Override
    protected void invokeMethod(Request request, ActorRef parent) {
        String operation = request.getOperation();

        if (StringUtils.equalsIgnoreCase(FrameworkHierarchyOperations.deleteFrameworkCache.name(), operation)) {
            String frameworkId = (String) request.get("identifier");
            TelemetryManager.info("Async deletion started for " + frameworkId);

            CompletableFuture.runAsync(() -> {
                try {
                    FrameworkCache.delete(frameworkId);
                    TelemetryManager.info("Async deletion completed for " + frameworkId);
                } catch (Exception e) {
                    TelemetryManager.error("Async deletion failed for " + frameworkId, e);
                }
            });
        } else {
            TelemetryManager.log("Unsupported operation: " + operation);
            throw new ClientException(LearningErrorCodes.ERR_INVALID_OPERATION.name(),
                    "Unsupported operation: " + operation);
        }
    }
}
