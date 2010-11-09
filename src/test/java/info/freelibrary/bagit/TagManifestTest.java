package info.freelibrary.bagit;

import static org.junit.Assert.fail;

import info.freelibrary.util.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

public class TagManifestTest {

	private static final String MANIFESTS_PATH = "src/test/resources/manifests/";

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		FileUtils.delete(new File(MANIFESTS_PATH));
	}

	@Test
	public void testTagManifest() {
		File dir1 = new File(MANIFESTS_PATH + "test1");
		File file1 = new File(dir1, "tagmanifest-md5.txt");

		try {
			dir1.mkdirs();
			file1.createNewFile();
			file1.deleteOnExit();
			dir1.deleteOnExit();

			new TagManifest(dir1);

			File dir2 = new File(MANIFESTS_PATH + "test2");
			File file2 = new File(dir2, "tagmanifest-sha1.txt");

			dir2.mkdirs();
			file2.createNewFile();
			file2.deleteOnExit();
			dir2.deleteOnExit();

			new TagManifest(dir2);
		}
		catch (IOException details) {
			fail(details.getMessage());
		}
	}

}
