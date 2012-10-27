package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.StringUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagDataTest {

	private static final String BAG_DIR = "src/test/resources/bags/";

	private static final String WORK_DIR = "src/test/resources/bagDataTests/";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BagDataTest.class);

	private static Bag myBag;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty(Bag.WORK_DIR, WORK_DIR);
		myBag = new Bag(BAG_DIR + "dryad_630");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		FileUtils.delete(new File(WORK_DIR));
	}

	@Test
	public void testGetAsUTF8StringString() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Running testGetAsUTF8StringString");
		}

		try {
			BagData data = myBag.getBagData();
			String xml = data.getAsUTF8String("datafile-3/dryadfile-3.xml");

			if (!xml.startsWith("<?xml version")
					|| !xml.endsWith("</DryadDataFile>")) {
				Assert.fail(xml);
			}
		}
		catch (IOException details) {
			Assert.fail(details.getMessage());
		}
	}

	@Test
	public void testGetFilePaths() {
		String[] expected = new String[] { "dryadpub.xml",
				"datafile-5/ApineOPSIN.nexus", "datafile-5/dryadfile-5.xml",
				"datafile-1/ApineCYTB.nexus", "datafile-1/dryadfile-1.xml",
				"dryadpkg.xml", "datafile-3/dryadfile-3.xml",
				"datafile-3/Apine16S.nexus", "datafile-4/dryadfile-4.xml",
				"datafile-4/Apine28S.nexus", "datafile-2/dryadfile-2.xml",
				"datafile-2/ApineDNA.morph.nexus" };

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Running testGetFilePaths");
		}

		String[] paths = myBag.getBagData().getFilePaths();

		Arrays.sort(paths);
		Arrays.sort(expected);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Found paths: {}", StringUtils.toString(paths, '|'));
		}

		Assert.assertArrayEquals(paths, expected);
	}

	@Test
	public void testGetFilePathsString() {
		String[] expected = new String[] { "dryadpub.xml",
				"datafile-5/dryadfile-5.xml", "datafile-1/dryadfile-1.xml",
				"dryadpkg.xml", "datafile-3/dryadfile-3.xml",
				"datafile-4/dryadfile-4.xml", "datafile-2/dryadfile-2.xml" };

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Running testGetFilePathsRegex");
		}

		BagData data = myBag.getBagData();
		String[] paths = data.getFilePaths("^d.*");

		Arrays.sort(paths);
		Arrays.sort(expected);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Found paths: {}", StringUtils.toString(paths, '|'));
		}

		Assert.assertArrayEquals(paths, expected);
	}

	@Test
	public void testGetFilePathsStringString() {
		String[] expected = new String[] { "datafile-5/dryadfile-5.xml",
				"datafile-1/dryadfile-1.xml", "datafile-3/dryadfile-3.xml",
				"datafile-4/dryadfile-4.xml", "datafile-2/dryadfile-2.xml" };

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Running testGetFilePathsRegexRegex");
		}

		BagData data = myBag.getBagData();
		String[] paths = data.getFilePaths("^datafile.*", ".*\\.xml");

		Arrays.sort(paths);
		Arrays.sort(expected);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Found paths: {}", StringUtils.toString(paths, '|'));
		}

		Assert.assertArrayEquals(paths, expected);
	}
}
