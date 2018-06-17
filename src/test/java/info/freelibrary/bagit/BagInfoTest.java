
package info.freelibrary.bagit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

public class BagInfoTest {

    private static final String EOL = System.getProperty("line.separator");

    private static final String WORK_DIR = "target/tests/bag-info-tests";

    private static final String EMAIL = "ksclarke@ksclarke.io";

    private static final String NAME = "Kevin S. Clarke";

    private static final String ORG = "FreeLibrary.INFO";

    private static final Logger LOGGER = LoggerFactory.getLogger(BagInfoTest.class, Constants.BUNDLE_NAME);

    /**
     * Cleans up after tests.
     *
     * @throws Exception If there is trouble cleaning up
     */
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        new File(WORK_DIR).delete();
    }

    /**
     * Sets up the tests.
     *
     * @throws Exception If there is trouble setting up for the tests
     */
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        new File(WORK_DIR).mkdirs();
    }

    /**
     * Tests creating bag info from properties.
     */
    @Test
    public void testBagInfoProperties() {
        final Properties properties = new Properties();
        final BagInfo bagInfo;

        properties.setProperty(BagInfoTags.CONTACT_EMAIL, EMAIL);
        properties.setProperty(BagInfoTags.CONTACT_NAME, NAME);
        properties.setProperty(BagInfoTags.SOURCE_ORG, ORG);

        bagInfo = new BagInfo(properties);

        assertEquals(bagInfo.getValue(BagInfoTags.CONTACT_EMAIL), EMAIL);
        assertEquals(bagInfo.getValue(BagInfoTags.CONTACT_NAME), NAME);
        assertEquals(bagInfo.getValue(BagInfoTags.SOURCE_ORG), ORG);

        assertEquals(3, bagInfo.countTags());
    }

    /**
     * Tests setting metadata.
     */
    @Test
    public void testSetMetadata() {
        final BagInfo bagInfo = new BagInfo();

        bagInfo.addMetadata(BagInfoTags.CONTACT_EMAIL, EMAIL);
        bagInfo.addMetadata(BagInfoTags.CONTACT_NAME, NAME);

        assertEquals(bagInfo.getValue(BagInfoTags.CONTACT_EMAIL), EMAIL);
        assertEquals(bagInfo.getValue(BagInfoTags.CONTACT_NAME), NAME);
        assertEquals(bagInfo.countTags(), 2);
    }

    /**
     * Tests getting value.
     */
    @Test
    public void testGetValueString() {
        final BagInfo bagInfo = new BagInfo();

        bagInfo.addMetadata(BagInfoTags.CONTACT_EMAIL, EMAIL);
        assertEquals(EMAIL, bagInfo.getValue(BagInfoTags.CONTACT_EMAIL));
    }

    /**
     * Tests removing metadata.
     */
    @Test
    public void testRemoveMetadata() {
        final BagInfo bagInfo = new BagInfo();
        bagInfo.addMetadata(BagInfoTags.CONTACT_EMAIL, EMAIL);
        assertEquals(bagInfo.countTags(), 1);
        bagInfo.removeMetadata(BagInfoTags.CONTACT_EMAIL);
        assertEquals(bagInfo.countTags(), 0);
    }

    /**
     * Tests getting value.
     */
    @Test
    public void testGetValueStringString() {
        final String email = "thetrashcan@gmail.com";
        final BagInfo bagInfo = new BagInfo();
        String result;

        bagInfo.addMetadata(BagInfoTags.CONTACT_EMAIL, EMAIL);
        result = bagInfo.getValue(BagInfoTags.CONTACT_EMAIL, email);
        assertEquals(result, EMAIL);

        bagInfo.removeMetadata(BagInfoTags.CONTACT_EMAIL);
        result = bagInfo.getValue(BagInfoTags.CONTACT_EMAIL, email);
        assertEquals(result, email);
    }

    /**
     * Tests getting tags.
     */
    @Test
    public void testGetTags() {
        final BagInfo bagInfo = new BagInfo();

        bagInfo.addMetadata(BagInfoTags.CONTACT_EMAIL, EMAIL);
        bagInfo.addMetadata(BagInfoTags.CONTACT_NAME, NAME);
        bagInfo.addMetadata(BagInfoTags.ORG_ADDRESS, ORG);
        assertEquals(3, bagInfo.countTags());

        final String[] tags = bagInfo.getTags();

        for (final String tag : tags) {
            if (!bagInfo.containsTag(tag)) {
                fail(LOGGER.getMessage(MessageCodes.BAGIT_061, tag));
            }
        }

        assertEquals(3, tags.length);
    }

    /**
     * Tests writing to file.
     */
    @Test
    public void testWriteToFile() {
        final File testBagInfo = new File(WORK_DIR, BagInfo.FILE_NAME);
        final BagInfo bagInfo = new BagInfo();

        bagInfo.addMetadata(BagInfoTags.ORG_ADDRESS, ORG);
        bagInfo.addMetadata(BagInfoTags.CONTACT_EMAIL, EMAIL);
        bagInfo.addMetadata(BagInfoTags.CONTACT_NAME, NAME);

        try {
            bagInfo.writeTo(testBagInfo);
        } catch (final IOException details) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(details.getMessage(), details);
            }

            fail(details.getMessage());
        }

        try {
            final FileReader fileReader = new FileReader(testBagInfo);
            final BufferedReader reader = new BufferedReader(fileReader);
            final StringBuilder buffer = new StringBuilder();
            String expected = new String();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append(EOL);
            }

            reader.close();

            expected += "Organization-Address: " + ORG + EOL;
            expected += "Contact-Email: " + EMAIL + EOL;
            expected += "Contact-Name: " + NAME + EOL;

            assertEquals(expected.toString(), buffer.toString());
        } catch (final IOException details) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(details.getMessage(), details);
            }

            fail(details.getMessage());
        }
    }

    /**
     * Test reading from bag info.
     */
    @Test
    public void testReadFrom() {
        try {
            final BagInfo bagInfo = new BagInfo(new File("src/test/resources/files/baginfo"));
            final String contactName = bagInfo.getValue(BagInfoTags.CONTACT_NAME);
            final String sourceOrg = bagInfo.getValue(BagInfoTags.SOURCE_ORG);
            final String email = bagInfo.getValue(BagInfoTags.CONTACT_EMAIL);

            assertEquals(ORG, sourceOrg);
            assertEquals(EMAIL, email);
            assertEquals(NAME, contactName);
            assertEquals(3, bagInfo.countTags());
        } catch (final IOException details) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(details.getMessage(), details);
            }

            fail(details.getMessage());
        }
    }
}
