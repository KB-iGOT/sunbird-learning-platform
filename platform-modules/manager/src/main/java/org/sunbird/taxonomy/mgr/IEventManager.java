package org.sunbird.taxonomy.mgr;

import org.sunbird.common.dto.Response;


import java.util.Map;

/**
 * The Interface IContentManager is the Contract for the operations that can be
 * perform on Content Node in the Graph. Including all Low (CRUD) Level and
 * high-level operations.
 * 
 * The sub-class implementing these operations should take care of uploading the
 * artifacts or assets to the respective Storage Space.
 * 
 * @author Azhar
 * @see org.sunbird.taxonomy.mgr.impl.EventManagerImpl
 */
public interface IEventManager {



	/**
	 * Publish is High level Content Operation mainly deals with the tasks
	 * needed for making any content in <code>LIVE</code> state. It includes the
	 * downloading of all the <code>assets</code> and <code>icons</code> to the
	 * storage space, replace the <code>URLs</code> with relative Urls, set the
	 * <code>body</code> of content object. Finally Creates the
	 * <code>ECAR</code> and upload the package to Storage Space, update the
	 * package version information, sets the <code>downloadUrl</code> property.
	 * 
	 * <p>
	 * It is a <code>Pipelined Operation</code> which is accomplished by several
	 * <code>Processors</code> meant for atomic tasks.
	 * 
	 * <p>
	 * A subclass must provide an implementation of this method.
	 *
	 * @param contentId
	 *            the content <code>identifier</code> which needs to be publish.
	 * @param requestMap
	 *            the map of request params
	 * @return the response contains the ECAR <code>URL</code> in its Result Set
	 */
	Response publish(String contentId, Map<String, Object> requestMap);


}