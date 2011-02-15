package info.freelibrary.bagit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import info.freelibrary.util.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayloadManifestTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(PayloadManifestTest.class);

	private static final String BAGS_DIR = "src/test/resources/bags/";

	private static final String WORK_DIR = "src/test/resources/bagTests";
	
	private static final String FILE_DIR = "src/test/resources/files/";
	
	private static Bag BAG;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty(Bag.WORK_DIR, WORK_DIR);
		BAG = new Bag(BAGS_DIR + "dryad_630");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		FileUtils.delete(new File(WORK_DIR));
	}

	@Test
	public void testPayloadManifest() {
		try {
			PayloadManifest manifest = new PayloadManifest(BAG.myDir);
			Assert.assertEquals(12, manifest.countEntries());
		}
		catch (IOException details) {
			fail(details.getMessage());
		}
	}

	@Test
	public void testUpdateWith() {
		try {
			PayloadManifest manifest = new PayloadManifest(BAG.myDir);
			manifest.updateWith(new File(FILE_DIR + "package-mets.xml"));
			Assert.assertEquals(13, manifest.countEntries());
		}
		catch (IOException details) {
			fail(details.getMessage());
		}
	}

}
