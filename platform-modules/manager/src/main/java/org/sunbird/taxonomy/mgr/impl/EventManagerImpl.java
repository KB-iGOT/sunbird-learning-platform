package org.sunbird.taxonomy.mgr.impl;

import org.springframework.stereotype.Component;
import org.sunbird.common.dto.Response;
import org.sunbird.content.mgr.impl.*;
import org.sunbird.taxonomy.mgr.IContentManager;
import org.sunbird.taxonomy.mgr.IEventManager;

import java.util.Map;

/**
 * The Class <code>ContentManagerImpl</code> is the implementation of
 * <code>IContentManager</code> for all the operation including CRUD operation
 * and High Level Operations. This implementation intern calls for
 * <code>IMimeTypeManager</code> implementation based on the
 * <code>MimeType</code>. For <code>Bundle</code> implementation it is directly
 * backed by Content Work-Flow Pipeline and other High Level implementation is
 * backed by the implementation of <code>IMimeTypeManager</code>.
 *
 * @author Azhar
 * @see IContentManager
 */
@Component
public class EventManagerImpl extends BaseContentManager implements IEventManager {

    private final EventManager eventManager = new EventManager();

    /*
     * (non-Javadoc)
     *
     * @see org.sunbird.taxonomy.mgr.IContentManager#publish(java.lang.String,
     * java.lang.String)
     */
    @Override
    public Response publish(String contentId, Map<String, Object> requestMap) {
        return this.eventManager.publish(contentId, requestMap);
    }


}