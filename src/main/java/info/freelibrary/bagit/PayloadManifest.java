package info.freelibrary.bagit;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.RegexFileFilter;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A manifest for data files conveyed in the bag's data directory.
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
class PayloadManifest extends AbstractManifest {

	private static final String FILE_NAME = "manifest-{}.txt";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PayloadManifest.class);

	PayloadManifest(File aBagDir) throws IOException {
		super(aBagDir);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.create_manifest"));
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected String getNamePattern() {
		return FILE_NAME;
	}

	/**
	 * Returns an XML representation of the <code>PayloadManifest</code>.
	 */
	public String toString() {
		String eol = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();

		builder.append("<dataManifest>").append(eol).append(super.toString());
		builder.append("</dataManifest>").append(eol);

		return builder.toString();
	}

	/**
	 * Updates the manifest with the supplied file(s) -- &quot;files&quot; in
	 * the case where <code>aFile</code> is a directory.
	 * 
	 * @param aFile A file or directory to be added to the manifest
	 * @throws IOException If there is a problem reading or writing the payload files
	 */
	void updateWith(File aFile) throws IOException {
		RegexFileFilter fileFilter = new RegexFileFilter(".*");

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.add_data", aFile));
		}
		
		for (File file : FileUtils.listFiles(aFile, fileFilter, true)) {
			try {
				add(file);
			}
			catch (NoSuchAlgorithmException details) {
				throw new RuntimeException(details); // should not happen
			}
		}
	}
}
