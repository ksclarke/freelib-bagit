
package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import info.freelibrary.util.FileUtils;

/**
 * These tests mostly just to test the examples' syntax; testing functionality is done by the regular unit tests.
 */
public class ExamplesTest {

    private static final String WORK_DIR = "target/tests/examples-tests";

    private static final String DIR_BAG = "src/test/resources/bags/dryad_630";

    private static final String DIR_BAG_DATA_DIR = DIR_BAG + "/data/";

    private static final String TAR_GZ_BAG = "src/test/resources/bags/dryad_630.tar.gz";

    /**
     * Sets up tests.
     *
     * @throws Exception If there is trouble setting up the tests
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(Bag.WORK_DIR, WORK_DIR);
    }

    /**
     * Cleans up after tests.
     *
     * @throws Exception If there is trouble cleaning up
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        FileUtils.delete(new File(WORK_DIR));
    }

    /**
     * Tests related to creating bags. These are from examples from the documentation.
     */
    @SuppressWarnings("unused")
    @Test
    public void testCreateExamples() {
        try {
            final Bag bag1 = new Bag("new_bag");
            final Bag bag2 = new Bag(DIR_BAG);
            final Bag bag3 = new Bag(TAR_GZ_BAG);
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests for the examples that add metadata.
     */
    @Test
    public void testAddMetadata() {
        try {
            final Bag bag = new Bag("new_bag_metadata");
            final BagInfo bagInfo = bag.getBagInfo();

            bagInfo.addMetadata(BagInfoTags.CONTACT_NAME_TAG, "Kevin S. Clarke");
            bagInfo.addMetadata(BagInfoTags.CONTACT_PHONE_TAG, "ksclarke@ksclarke.io");
            bagInfo.addMetadata("Alt-Email", "thetrashcan@gmail.com");
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests for the examples that add metadata properties.
     */
    @Test
    public void testAddMetadataProperties() {
        try {
            final Properties metadata = new Properties();
            final File properties = new File("src/test/resources/files/test.properties");
            try {
                metadata.load(new FileReader(properties));
            } catch (final IOException details) {
                fail(details.getMessage());
            }

            @SuppressWarnings("unused")
            final Bag bag = new Bag("bag_for_metadata", metadata);
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests for the examples that add data.
     */
    @Test
    public void testAddData() {
        try {
            final Bag bag = new Bag("dryad_630");
            final File dataFile = new File(DIR_BAG_DATA_DIR + "datafile-1/ApineCYTB.nexus");
            final File dataDir = new File(DIR_BAG_DATA_DIR + "datafile-2/");

            bag.addData(dataFile, dataDir);
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests for the examples that test bag validity.
     */
    @Test
    public void testValidate() {
        try {
            final Bag bag = new Bag(TAR_GZ_BAG);

            try {
                new BagValidator().validate(bag);
            } catch (final BagException details) {
                fail(details.getMessage());
            }
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests that check bag validity.
     */
    @Test
    public void testIsValid() {
        try {
            final Bag bag = new Bag(TAR_GZ_BAG);

            if (new BagValidator().isValid(bag)) {
                // do nothing...
            } else {
                fail("Bag isn't valid");
            }
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests for the examples that write bags.
     */
    @Test
    public void testWriteBag() {
        try {
            final Bag bag = new Bag(DIR_BAG);

            try {
                new BagValidator().validate(bag).toTarBZip2();
            } catch (final BagException details) {
                fail(details.getMessage());
            } catch (final IOException details) {
                fail(details.getMessage());
            }
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }
}
