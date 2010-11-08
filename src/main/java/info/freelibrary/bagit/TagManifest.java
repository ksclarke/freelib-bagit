package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TagManifest extends AbstractManifest {

	private static final String FILE_NAME = "tagmanifest-{}.txt";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TagManifest.class);

	TagManifest(File aBagDir) throws IOException {
		super(aBagDir);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.tag_manifest"));
		}
	}

	/**
	 * Returns an XML representation of the <code>TagManifest</code>.
	 */
	public String toString() {
		String eol = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();
		
		builder.append("<tagManifest>").append(eol).append(super.toString());
		builder.append("</tagManifest>").append(eol);
		
		return builder.toString();
	}
	
	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected String getNamePattern() {
		return FILE_NAME;
	}
}
