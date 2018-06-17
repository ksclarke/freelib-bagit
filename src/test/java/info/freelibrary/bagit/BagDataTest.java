
package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

public class BagDataTest {

    private static final String BAG_DIR = "src/test/resources/bags/";

    private static final String WORK_DIR = "target/tests/bag-data-tests/";

    private static final Logger LOGGER = LoggerFactory.getLogger(BagDataTest.class, Constants.BUNDLE_NAME);

    private static final String[] EXPECTED = new String[] { "dryadpub.xml", "datafile-5/ApineOPSIN.nexus",
        "datafile-5/dryadfile-5.xml", "datafile-1/ApineCYTB.nexus", "datafile-1/dryadfile-1.xml", "dryadpkg.xml",
        "datafile-3/dryadfile-3.xml", "datafile-3/Apine16S.nexus", "datafile-4/dryadfile-4.xml",
        "datafile-4/Apine28S.nexus", "datafile-2/dryadfile-2.xml", "datafile-2/ApineDNA.morph.nexus" };

    private static Bag myBag;

    /**
     * Sets up for tests.
     *
     * @throws Exception If there is trouble with the setup
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(Bag.WORK_DIR, WORK_DIR);
        myBag = new Bag(BAG_DIR + "dryad_630");
    }

    /**
     * Cleans up after tests.
     *
     * @throws Exception If there is trouble with the clean up
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        FileUtils.delete(new File(WORK_DIR));
    }

    /**
     * Testing getting as UTF-8 string.
     */
    @Test
    public void testGetAsUTF8StringString() {
        try {
            final BagData data = myBag.getBagData();
            final String xml = data.getAsUTF8String(EXPECTED[5]);
            // here
            if (!xml.startsWith("<?xml version") || !xml.endsWith("</DryadDataPackage>")) {
                Assert.fail(xml);
            }
        } catch (final IOException details) {
            Assert.fail(details.getMessage());
        }
    }

    /**
     * Test get file paths.
     */
    @Test
    public void testGetFilePaths() {
        final String[] paths = myBag.getBagData().getFilePaths();
        final String[] expected = EXPECTED.clone();

        Arrays.sort(paths);
        Arrays.sort(expected);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_004, StringUtils.toString(paths, '|'));
        }

        Assert.assertArrayEquals(paths, expected);
    }

    /**
     * Tests getting file path as string.
     */
    @Test
    public void testGetFilePathsString() {
        final String[] expected = new String[] { EXPECTED[0], EXPECTED[2], EXPECTED[4], EXPECTED[5], EXPECTED[6],
            EXPECTED[8], EXPECTED[10] };
        final BagData data = myBag.getBagData();
        final String[] paths = data.getFilePaths("^d.*");

        Arrays.sort(paths);
        Arrays.sort(expected);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_004, StringUtils.toString(paths, '|'));
        }

        Assert.assertArrayEquals(paths, expected);
    }

    /**
     * Tests getting file path as string.
     */
    @Test
    public void testGetFilePathsStringString() {
        final String[] expected = new String[] { EXPECTED[2], EXPECTED[4], EXPECTED[6], EXPECTED[8], EXPECTED[10] };
        final BagData data = myBag.getBagData();
        final String[] paths = data.getFilePaths("^datafile.*", ".*\\.xml");

        Arrays.sort(paths);
        Arrays.sort(expected);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_004, StringUtils.toString(paths, '|'));
        }

        Assert.assertArrayEquals(paths, expected);
    }
}
