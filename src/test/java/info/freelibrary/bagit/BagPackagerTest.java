package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import info.freelibrary.util.I18nObject;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagPackagerTest extends I18nObject implements BagConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(BagPackagerTest.class);
	
	private static final String SOURCE_BAG_PATH = "src/test/resources/dryad_630";
	
	@Before
	public void setUp() throws Exception {
		System.setProperty(BAGIT_WORK_DIR_PROPERTY, "src/test/resources/packager");
	}

	@Test
	public void testToTar() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testToTar"));
		}
		
		try {
			Bag bag = new Bag(SOURCE_BAG_PATH);
			File file = BagPackager.toTar(bag);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.tar_written", file));
			}
			
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
	public void testFromTar() {
		File tarFile = new File("src/test/resources/packager/dryad_630.tar");
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testFromTar"));
		}
		
		try {
			Bag bag = BagPackager.fromTar(tarFile);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.created_bag", bag.myDir));
			}

			bag.finalize();
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
		
		if (!tarFile.delete()) {
			fail(getI18n("bagit.test.failed_tar_delete", tarFile));
		}
		else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.tar_deleted", tarFile));
		}
	}
	
	@Test
	public void testToZip() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testToZip"));
		}
		
		try {
			Bag bag = new Bag(SOURCE_BAG_PATH);
			File zipFile = BagPackager.toZip(bag);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.zip_written", zipFile));
			}
			
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
	public void testFromZip() {
		File zipFile = new File("src/test/resources/packager/dryad_630.zip");
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testFromZip"));
		}
		
		try {
			Bag bag = BagPackager.fromZip(zipFile);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.created_bag", bag.myDir));
			}

			bag.finalize();
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
		
		if (!zipFile.delete()) {
			fail(getI18n("bagit.test.failed_zip_delete", zipFile));
		}
		else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.zip_deleted", zipFile));
		}
	}
	
	@Test
	public void testToTarBZip2() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testToTarBZip2"));
		}
		
		try {
			Bag bag = new Bag(SOURCE_BAG_PATH);
			BagPackager.toTarBZip2(bag);
			
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
	public void testFromTarBZip2() {
		File tarBz2File = new File("src/test/resources/packager/dryad_630.tar.bz2");
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testFromTarBZip2"));
		}
		
		try {
			Bag bag = BagPackager.fromTarBZip2(tarBz2File);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.created_bag", bag.myDir));
			}

			bag.finalize();
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
		
		if (!tarBz2File.delete()) {
			fail(getI18n("bagit.test.failed_bz2_delete", tarBz2File));
		}
		else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.bz2_deleted", tarBz2File));
		}
	}

	@Test
	public void testToTarGz() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testToTarGz"));
		}
		
		try {
			Bag bag = new Bag(SOURCE_BAG_PATH);
			BagPackager.toTarGz(bag);
			
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
	public void testFromTarGz() {
		File tarGzFile = new File("src/test/resources/packager/dryad_630.tar.gz");
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.starting_test", "testFromTarGz"));
		}
		
		try {
			Bag bag = BagPackager.fromTarGz(tarGzFile);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.created_bag", bag.myDir));
			}

			bag.finalize();
		}
		catch (Throwable throwable) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			
			fail(throwable.getMessage());
		}
		
		if (!tarGzFile.delete()) {
			fail(getI18n("bagit.test.failed_gz_delete", tarGzFile));
		}
		else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.test.gz_deleted", tarGzFile));
		}
	}

}
