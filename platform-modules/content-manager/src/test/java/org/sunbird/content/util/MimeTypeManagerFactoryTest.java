package org.sunbird.content.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sunbird.content.mimetype.mgr.IMimeTypeManager;
import org.sunbird.content.mimetype.mgr.impl.DocumentMimeTypeManager;

public class MimeTypeManagerFactoryTest {

	@Test
	public void testGetManagerForHtmlMimeTypeReturnsDocumentManager() {
		IMimeTypeManager manager = MimeTypeManagerFactory.getManager("Content", "application/vnd.ekstep.html");
		assertNotNull(manager);
		assertTrue(manager instanceof DocumentMimeTypeManager);
	}

	@Test
	public void testGetManagerForHtmlMimeTypeSameInstanceAsPdf() {
		IMimeTypeManager htmlManager = MimeTypeManagerFactory.getManager("Content", "application/vnd.ekstep.html");
		IMimeTypeManager pdfManager = MimeTypeManagerFactory.getManager("Content", "application/pdf");
		assertSame(pdfManager, htmlManager);
	}

	@Test
	public void testGetManagerForHtmlArchiveStillRoutesToHtmlMimeTypeMgr() {
		IMimeTypeManager htmlArchiveManager = MimeTypeManagerFactory.getManager("Content",
				"application/vnd.ekstep.html-archive");
		IMimeTypeManager htmlManager = MimeTypeManagerFactory.getManager("Content", "application/vnd.ekstep.html");
		assertTrue(htmlArchiveManager != htmlManager);
	}
}
