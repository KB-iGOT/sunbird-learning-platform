package org.sunbird.taxonomy.controller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.Slug;
import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.taxonomy.mgr.IContentManager;
import org.sunbird.taxonomy.mgr.IEventManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import javax.ws.rs.PathParam;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class ContentV3Controller, is the main entry point for the High Level
 * Content Operations, mostly it holds the API Method related to Content
 * Workflow Management such as 'Upload', 'Publish' 'Optimize', 'Extract' and
 * 'Bundle'. Other that these operation the Content can have other basic CRUD
 * Operations.
 * <p>
 * All the Methods are backed by their corresponding managers, which have the
 * actual logic to communicate with the middleware and core level APIs.
 *
 * @author Azhar
 */
@Controller
@RequestMapping("/event/v4")
public class EventV4Controller extends BaseController {

    @Autowired
    private IEventManager eventManager;

    private static final String CHANNEL_ID = "X-Channel-Id";

    private String UNDERSCORE = "_";

    private String DOT = ".";

    private List<String> preSignedObjTypes = Arrays.asList("assets", "artifact", "hierarchy");


    /**
     * This method carries all the tasks related to 'Publish' operation of
     * content work-flow.
     *
     * @param contentId The Content Id which needs to be published.
     * @return The Response entity with Content Id and ECAR URL in its Result
     * Set.
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = {"/publish/{id:.+}", "/public/publish/{id:.+}"}, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> publish(@PathVariable(value = "id") String contentId,
                                            @RequestBody Map<String, Object> map) {
        String apiId = "ekstep.learning.content.publish";
        Response response;
        TelemetryManager.log("Publish content | Content Id : " + contentId);
        try {
            TelemetryManager
                    .log("Calling the Manager for 'Publish' Operation | [Content Id " + contentId + "]" + contentId);
            Request request = getRequest(map);
            Map<String, Object> requestMap = (Map<String, Object>) request.getRequest().get("event");
            requestMap.put("publish_type", ContentWorkflowPipelineParams.Public.name().toLowerCase());

            if (null == requestMap.get("lastPublishedBy")
                    || StringUtils.isBlank(requestMap.get("lastPublishedBy").toString())) {
                return getExceptionResponseEntity(
                        new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_PUBLISHER.name(),
                                "Publisher User Id is blank"),
                        apiId, null);
            }

            response = eventManager.publish(contentId, requestMap);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}