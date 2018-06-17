
package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.RegexFileFilter;

/**
 * A manifest for data files conveyed in the bag's data directory.
 */
class PayloadManifest extends AbstractManifest {

    private static final String FILE_NAME = "manifest-{}.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(PayloadManifest.class, Constants.BUNDLE_NAME);

    PayloadManifest(final File aBagDir) throws IOException {
        super(aBagDir);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_032);
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
    @Override
    public String toString() {
        final String eol = System.getProperty("line.separator");

        return new StringBuilder(40).append("<dataManifest>").append(eol).append(super.toString()).append(
                "</dataManifest>").append(eol).toString();
    }

    /**
     * Updates the manifest with the supplied file(s) -- &quot;files&quot; in the case where <code>aFile</code> is a
     * directory.
     *
     * @param aFile A file or directory to be added to the manifest
     * @throws IOException If there is a problem reading or writing the payload files
     */
    void updateWith(final File aFile) throws IOException {
        final RegexFileFilter fileFilter = new RegexFileFilter(".*");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_029, aFile);
        }

        for (final File file : FileUtils.listFiles(aFile, fileFilter, true)) {
            try {
                add(file);
            } catch (final NoSuchAlgorithmException details) {
                throw new I18nRuntimeException(details);
            }
        }
    }
}
