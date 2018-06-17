
package info.freelibrary.bagit;

import java.io.File;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * Resolves resources in the <code>fetch.txt</code> file found in a <code>Bag</code>.
 */
class Fetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Fetcher.class, Constants.BUNDLE_NAME);

    Fetcher(final File aFile) {
        LOGGER.debug(aFile.getAbsolutePath());
    }
}
