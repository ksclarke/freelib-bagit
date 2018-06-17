
package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.util.FileUtils;

public class ValidBagTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.BUNDLE_NAME);

    private static final String WORK_DIR = "target/tests/valid-bag-tests";

    /**
     * Sets up the tests.
     *
     * @throws Exception If the tests cannot be successfully set up
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(Bag.WORK_DIR, WORK_DIR);
    }

    /**
     * Cleans up after tests.
     *
     * @throws Exception If the tests cannot be cleaned up.
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        FileUtils.delete(new File(WORK_DIR));
    }

    /**
     * Tests creating a valid bag.
     */
    @Test
    public void testValidBag() {
        try {
            final Bag bag = new Bag("papas_got_a_brand_new").complete();
            final ValidBag validBag = new BagValidator().validate(bag);

            validBag.toDir();
        } catch (final IOException details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(details.getMessage(), details);
            }

            fail(details.getMessage());
        } catch (final BagException details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(details.getMessage(), details);
            }

            fail(details.getMessage());
        }
    }

}
