
package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * A manifest for describing metadata files contained within the bag.
 */
class TagManifest extends AbstractManifest {

    private static final String FILE_NAME = "tagmanifest-{}.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(TagManifest.class, Constants.BUNDLE_NAME);

    TagManifest(final File aBagDir) throws IOException {
        super(aBagDir);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_033);
        }
    }

    /**
     * Returns an XML representation of the <code>TagManifest</code>.
     */
    @Override
    public String toString() {
        final String eol = System.getProperty("line.separator");

        return new StringBuilder(40).append("<tagManifest>").append(eol).append(super.toString()).append(
                "</tagManifest>").append(eol).toString();
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
