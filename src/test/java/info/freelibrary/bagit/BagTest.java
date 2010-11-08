package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import info.freelibrary.util.I18nObject;

import java.io.File;
import java.io.IOException;

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
			Bag bag = new Bag(new File("src/test/resources/dryad_630"));
			bag.finalize();
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
			Bag bag = new Bag(new File(BAGS_DIR + "dryad_630.tar"));
			bag.finalize();
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
	}
}
