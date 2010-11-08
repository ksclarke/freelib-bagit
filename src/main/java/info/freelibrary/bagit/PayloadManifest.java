package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
