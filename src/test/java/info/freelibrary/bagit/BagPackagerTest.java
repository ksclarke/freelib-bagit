package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nObject;

import java.io.File;

import org.junit.Before;
import org.junit.AfterClass;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagPackagerTest extends I18nObject {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BagPackagerTest.class);

	private static final String PACKAGER_TEST_DIR = "src/test/resources/packager";

	private static final String SOURCE_BAG_PATH = "src/test/resources/dryad_630";

	@Before
	public void setUp() throws Exception {
		System.setProperty(Bag.WORK_DIR, PACKAGER_TEST_DIR);
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		FileUtils.delete(new File(PACKAGER_TEST_DIR));
	}

	@Test
	public void testToTar() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testToTar"));
		}

		try {
			File file = BagPackager.toTar(new Bag(SOURCE_BAG_PATH));

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.tar_written", file));
			}
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}

	@Test
	public void testFromTar() {
		File tarFile = new File("src/test/resources/packages/dryad_630.tar");

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testFromTar"));
		}

		try {
			Bag bag = BagPackager.fromTar(tarFile);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.created_bag", bag.myDir));
			}
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}

	@Test
	public void testToZip() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testToZip"));
		}

		try {
			File zipFile = BagPackager.toZip(new Bag(SOURCE_BAG_PATH));

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.zip_written", zipFile));
			}
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}

	@Test
	public void testFromZip() {
		File zipFile = new File("src/test/resources/packages/dryad_630.zip");

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testFromZip"));
		}

		try {
			Bag bag = BagPackager.fromZip(zipFile);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.created_bag", bag.myDir));
			}
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}

	@Test
	public void testToTarBZip2() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testToTarBZip2"));
		}

		try {
			BagPackager.toTarBZip2(new Bag(SOURCE_BAG_PATH));
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}

	@Test
	public void testFromTarBZip2() {
		File tarBz2File = new File(
				"src/test/resources/packages/dryad_630.tar.bz2");

		if (LOGGER.isDebugEnabled()) {
			LOGGER
					.debug(getI18n("bagit.test.starting_test",
							"testFromTarBZip2"));
		}

		try {
			Bag bag = BagPackager.fromTarBZip2(tarBz2File);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.created_bag", bag.myDir));
			}
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}

	@Test
	public void testToTarGz() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testToTarGz"));
		}

		try {
			BagPackager.toTarGz(new Bag(SOURCE_BAG_PATH));
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}

	@Test
	public void testFromTarGz() {
		File tarGzFile = new File(
				"src/test/resources/packages/dryad_630.tar.gz");

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testFromTarGz"));
		}

		try {
			Bag bag = BagPackager.fromTarGz(tarGzFile);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.created_bag", bag.myDir));
			}
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}

			fail(throwable.getMessage());
		}
	}

}
