
package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

public class BagPackagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BagPackagerTest.class, Constants.BUNDLE_NAME);

    private static final String PACKAGER_TEST_DIR = "target/test-working-dir";

    private static final String SOURCE_BAG_PATH = "src/test/resources/dryad_630";

    /**
     * Setup before a test is run.
     *
     * @throws Exception If there is trouble setting up the test
     */
    @Before
    public void setUp() throws Exception {
        System.setProperty(Bag.WORK_DIR, PACKAGER_TEST_DIR);
    }

    /**
     * Clean up after tests have run.
     *
     * @throws Exception If there is trouble cleaning up
     */
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        FileUtils.delete(new File(PACKAGER_TEST_DIR));
    }

    /**
     * Test bag packager to tar.
     */
    @Test
    public void testToTar() {
        try {
            final File file = BagPackager.toTar(new Bag(SOURCE_BAG_PATH));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_057, file);
            }
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Test bag packager from tar.
     */
    @Test
    public void testFromTar() {
        final File tarFile = new File("src/test/resources/packages/dryad_630.tar");

        try {
            final Bag bag = BagPackager.fromTar(tarFile);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_060, bag.myDir);
            }
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Tests bag packer to zip.
     */
    @Test
    public void testToZip() {
        try {
            final File zipFile = BagPackager.toZip(new Bag(SOURCE_BAG_PATH));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_059, zipFile);
            }
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Tests bag packager from zip.
     */
    @Test
    public void testFromZip() {
        final File zipFile = new File("src/test/resources/packages/dryad_630.zip");

        try {
            final Bag bag = BagPackager.fromZip(zipFile);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_060, bag.myDir);
            }
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Tests bag packager to tar.bz2 file.
     */
    @Test
    public void testToTarBZip2() {
        try {
            BagPackager.toTarBZip2(new Bag(SOURCE_BAG_PATH));
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Test bag packager from a tar.bz2 file.
     */
    @Test
    public void testFromTarBZip2() {
        final File tarBz2File = new File("src/test/resources/packages/dryad_630.tar.bz2");

        try {
            final Bag bag = BagPackager.fromTarBZip2(tarBz2File);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_060, bag.myDir);
            }
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Tests bag packager to tar.gz file.
     */
    @Test
    public void testToTarGz() {
       try {
            BagPackager.toTarGz(new Bag(SOURCE_BAG_PATH));
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Tests bag packager from tar.gz file.
     */
    @Test
    public void testFromTarGz() {
        final File tarGzFile = new File("src/test/resources/packages/dryad_630.tar.gz");

        try {
            final Bag bag = BagPackager.fromTarGz(tarGzFile);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_060, bag.myDir);
            }
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }
}
