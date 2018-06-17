
package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import info.freelibrary.util.FileUtils;

public class TagManifestTest {

    private static final String MANIFESTS_PATH = "target/tests/manifests/";

    /**
     * Cleans up after tests.
     *
     * @throws Exception If there is trouble cleaning up after the tests
     */
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        FileUtils.delete(new File(MANIFESTS_PATH));
    }

    /**
     * Tests the tag manifest.
     */
    @Test
    public void testTagManifest() {
        final File dir1 = new File(MANIFESTS_PATH + "test1");
        final File file1 = new File(dir1, "tagmanifest-md5.txt");

        try {
            dir1.mkdirs();
            file1.createNewFile();
            file1.deleteOnExit();
            dir1.deleteOnExit();

            new TagManifest(dir1);

            final File dir2 = new File(MANIFESTS_PATH + "test2");
            final File file2 = new File(dir2, "tagmanifest-sha1.txt");

            dir2.mkdirs();
            file2.createNewFile();
            file2.deleteOnExit();
            dir2.deleteOnExit();

            new TagManifest(dir2);
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

}
