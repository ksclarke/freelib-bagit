package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import info.freelibrary.util.I18nObject;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagTest extends I18nObject implements BagConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(BagTest.class);

	private static final String BAGS_DIR = "src/test/resources/bags/";

	@Before
	public void setUp() throws Exception {
		System.setProperty(BAGIT_WORK_DIR_PROPERTY,
				"src/test/resources/bagTests");
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		new File("src/test/resources/bagTests").delete();
	}
	
	@Test
	public void testBagFile() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testBagFile"));
		}

		try {
			Bag bag = new Bag(new File(BAGS_DIR + "bagFile1"));
			fail(getI18n("bagit.test.directory", bag.myDir));
		}
		catch (IOException details) {}
	}

	@Test
	public void testBagDir() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testBagDir"));
		}

		try {
			new Bag(new File("src/test/resources/dryad_630"));
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}
	
	@Test
	public void testBagTar() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testBagTar"));
		}

		try {
			new Bag(new File(BAGS_DIR + "dryad_630.tar"));
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
	}
	
	@Test
	public void testBagTarGz() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testBagTarGz"));
		}

		try {
			new Bag(new File(BAGS_DIR + "dryad_630.tar.gz"));
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
	}
	
	@Test
	public void testBagTarBzip2() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testBagTarBzip2"));
		}

		try {
			new Bag(new File(BAGS_DIR + "dryad_630.tar.bz2"));
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
	}
	
	@Test
	public void testBagZip() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testBagTarGz"));
		}

		try {
			new Bag(new File(BAGS_DIR + "dryad_630.zip"));
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
	}
}
