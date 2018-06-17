package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import info.freelibrary.util.FileUtils;

import junit.framework.Assert;

public class PayloadManifestTest {

    private static final String BAGS_DIR = "src/test/resources/bags/";

    private static final String WORK_DIR = "target/tests/payload-manifest-tests";

    private static final String FILE_DIR = "src/test/resources/files/";

    private static Bag BAG;

    /**
     * Sets up tests.
     *
     * @throws Exception If there is trouble setting up the tests
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(Bag.WORK_DIR, WORK_DIR);
        BAG = new Bag(BAGS_DIR + "dryad_630");
    }

    /**
     * Cleans up after the tests.
     *
     * @throws Exception If there is trouble cleaning up after the tests
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        FileUtils.delete(new File(WORK_DIR));
    }

    /**
     * Tests payload manifest construction.
     */
    @Test
    public void testPayloadManifest() {
        try {
            final PayloadManifest manifest = new PayloadManifest(BAG.myDir);
            Assert.assertEquals(12, manifest.countEntries());
        }
        catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests updating a payload manifest.
     */
    @Test
    public void testUpdateWith() {
        try {
            final PayloadManifest manifest = new PayloadManifest(BAG.myDir);
            manifest.updateWith(new File(FILE_DIR + "package-mets.xml"));
            Assert.assertEquals(13, manifest.countEntries());
        }
        catch (final IOException details) {
            fail(details.getMessage());
        }
    }

}
