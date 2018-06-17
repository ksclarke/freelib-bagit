
package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

public class BagValidatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BagValidatorTest.class, Constants.BUNDLE_NAME);

    private static final String BAGS_DIR = "src/test/resources/bags/";

    private static final String BAGS_TEST_DIR = "target/tests/bag-validator-tests";

    private static BagValidator VALIDATOR;

    /**
     * Setup for running tests.
     *
     * @throws Exception If the setup fails
     */
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        System.setProperty(Bag.WORK_DIR, BAGS_TEST_DIR);
        VALIDATOR = new BagValidator();
    }

    /**
     * Cleanup after running tests.
     *
     * @throws Exception If the cleanup fails
     */
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        FileUtils.delete(new File(BAGS_TEST_DIR));
    }

    /**
     * Tests checking payload data files.
     */
    @Test
    public void testCheckPayloadDataFiles() {
        try {
            final Bag bag = new Bag(BAGS_DIR + "dryad_632");
            VALIDATOR.validate(bag);
        } catch (final IOException details) {
            fail(details.getMessage());
        } catch (final BagException details) {
            if (!details.getMessage().equals(LOGGER.getMessage(MessageCodes.BAGIT_018))) {
                fail(LOGGER.getMessage(MessageCodes.BAGIT_062, details.getMessage()));
            }
        }
    }

    @Test
    public void testCheckPayloadDataFileCount() {
        try {
            final Bag bag = new Bag(BAGS_DIR + "dryad_631");
            VALIDATOR.validate(bag);
            fail(LOGGER.getMessage(MessageCodes.BAGIT_018));
        } catch (final IOException details) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(details.getMessage(), details);
            }

            fail(details.getMessage());
        } catch (final BagException details) {
            final String message = details.getMessage();

            if (!details.getMessage().equals(LOGGER.getMessage(MessageCodes.BAGIT_018))) {
                // This is a successful catch....
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.BAGIT_064, message);
                }

                fail(message);
            }
        }
    }

    @Test
    public void testIsComplete() {
        // fail("Not yet implemented");
    }

    @Test
    public void testIsValid() {
        // fail("Not yet implemented");
    }

    @Test
    public void testValidate() {
        // fail("Not yet implemented");
    }

}
