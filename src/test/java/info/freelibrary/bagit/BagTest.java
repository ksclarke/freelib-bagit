
package info.freelibrary.bagit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

public class BagTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BagTest.class, Constants.BUNDLE_NAME);

    private static final String WORKING_DIR = "target/bagtest-tests";

    private static final String BAGS_DIR = "src/test/resources/bags/";

    /**
     * Sets up the test.
     *
     * @throws Exception If the setup fails
     */
    @Before
    public void setUp() throws Exception {
        System.setProperty(Bag.WORK_DIR, WORKING_DIR);
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception If the clean up fails
     */
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        new File(WORKING_DIR).delete();
    }

    /**
     * Test get payload.
     */
    @Test
    public void testGetPayloadOxum() {
        try {
            final Bag bag = new Bag(BAGS_DIR + "dryad_630");
            assertEquals("133387.12", bag.getPayloadOxum());
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Test creating bag from file.
     */
    @Test
    public void testBagFile() {
        try {
            final Bag bag = new Bag(new File(BAGS_DIR, "bagFile1"));
            fail(LOGGER.getMessage(MessageCodes.BAGIT_056, bag.myDir));
        } catch (final IOException details) {
            // Expected
        }
    }

    /**
     * Test creating bag from directory.
     */
    @Test
    public void testBagDir() {
        try {
            new Bag(new File("src/test/resources/dryad_630"));
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Test creating bag from tar file.
     */
    @Test
    public void testBagTar() {
        try {
            new Bag(new File(BAGS_DIR + "dryad_630.tar"));
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Test creating bag from tar.gz file
     */
    @Test
    public void testBagTarGz() {
        try {
            new Bag(new File(BAGS_DIR + "dryad_630.tar.gz"));
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Tests creating bag from tar.bz2 file.
     */
    @Test
    public void testBagTarBzip2() {
        try {
            new Bag(new File(BAGS_DIR + "dryad_630.tar.bz2"));
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }

    /**
     * Tests creating bag from zip file.
     */
    @Test
    public void testBagZip() {
        try {
            new Bag(new File(BAGS_DIR + "dryad_630.zip"));
        } catch (final Throwable throwable) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(throwable.getMessage(), throwable);
            }

            fail(throwable.getMessage());
        }
    }
}
