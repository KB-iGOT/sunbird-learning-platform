package org.sunbird.content.validator;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.graph.dac.model.Node;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpDownloadUtility.class })
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class ContentValidatorTest {

	private static final String HTML_MIMETYPE = "application/vnd.ekstep.html";

	private ContentValidator contentValidator = new ContentValidator();
	private File tempHtmlFile;

	@Before
	public void setUp() {
		contentValidator = new ContentValidator();
	}

	@After
	public void tearDown() {
		if (null != tempHtmlFile && tempHtmlFile.exists())
			tempHtmlFile.delete();
	}

	private Node getContentNode(String mimeType, String artifactUrl) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put(ContentWorkflowPipelineParams.name.name(), "Test HTML Content");
		metadata.put(ContentWorkflowPipelineParams.mimeType.name(), mimeType);
		if (null != artifactUrl)
			metadata.put(ContentWorkflowPipelineParams.artifactUrl.name(), artifactUrl);
		return new Node("domain", metadata);
	}

	@Test(expected = ClientException.class)
	public void testHtmlContentNodeMissingArtifactUrlThrowsException() {
		Node node = getContentNode(HTML_MIMETYPE, null);
		contentValidator.isValidContentNode(node);
	}

	@Test
	public void testHtmlContentNodeWithValidHtmlArtifactIsValid() throws Exception {
		tempHtmlFile = File.createTempFile("index", ".html");
		FileUtils.writeStringToFile(tempHtmlFile, "<html><head><title>Test</title></head><body>Test Content</body></html>");

		PowerMockito.mockStatic(HttpDownloadUtility.class);
		PowerMockito.when(HttpDownloadUtility.downloadFile(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString()))
				.thenReturn(tempHtmlFile);

		Node node = getContentNode(HTML_MIMETYPE, "https://example.com/artifact/index.html");
		assertTrue(contentValidator.isValidContentNode(node));
	}

	@Test(expected = ClientException.class)
	public void testHtmlContentNodeWithWrongExtensionArtifactThrowsException() throws Exception {
		tempHtmlFile = File.createTempFile("index", ".txt");
		FileUtils.writeStringToFile(tempHtmlFile, "<html><head><title>Test</title></head><body>Test Content</body></html>");

		PowerMockito.mockStatic(HttpDownloadUtility.class);
		PowerMockito.when(HttpDownloadUtility.downloadFile(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString()))
				.thenReturn(tempHtmlFile);

		Node node = getContentNode(HTML_MIMETYPE, "https://example.com/artifact/index.txt");
		contentValidator.isValidContentNode(node);
	}

	@Test(expected = ClientException.class)
	public void testHtmlContentNodeWithWrongContentTypeArtifactThrowsException() throws Exception {
		tempHtmlFile = File.createTempFile("index", ".html");
		// PDF magic bytes so Tika detects application/pdf instead of text/html
		FileUtils.writeStringToFile(tempHtmlFile, "%PDF-1.4\n%Fake PDF content for mimetype detection test");

		PowerMockito.mockStatic(HttpDownloadUtility.class);
		PowerMockito.when(HttpDownloadUtility.downloadFile(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString()))
				.thenReturn(tempHtmlFile);

		Node node = getContentNode(HTML_MIMETYPE, "https://example.com/artifact/index.html");
		contentValidator.isValidContentNode(node);
	}

	@Test
	public void testExceptionChecksValidHtmlFile() throws Exception {
		tempHtmlFile = File.createTempFile("index", ".html");
		FileUtils.writeStringToFile(tempHtmlFile, "<html><head><title>Test</title></head><body>Test Content</body></html>");
		assertTrue(contentValidator.exceptionChecks(HTML_MIMETYPE, tempHtmlFile));
	}

	@Test(expected = ClientException.class)
	public void testExceptionChecksWrongExtension() throws Exception {
		tempHtmlFile = File.createTempFile("index", ".txt");
		FileUtils.writeStringToFile(tempHtmlFile, "<html><head><title>Test</title></head><body>Test Content</body></html>");
		contentValidator.exceptionChecks(HTML_MIMETYPE, tempHtmlFile);
	}

	@Test(expected = ClientException.class)
	public void testExceptionChecksWrongContentType() throws Exception {
		tempHtmlFile = File.createTempFile("index", ".html");
		FileUtils.writeStringToFile(tempHtmlFile, "%PDF-1.4\n%Fake PDF content for mimetype detection test");
		contentValidator.exceptionChecks(HTML_MIMETYPE, tempHtmlFile);
	}
}
