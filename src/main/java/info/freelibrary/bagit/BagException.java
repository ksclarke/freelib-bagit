
package info.freelibrary.bagit;

import java.io.File;

import info.freelibrary.util.I18nRuntimeException;

/**
 * An optionally typed exception thrown during the bag validation process. It may contain a specific reason why the
 * validation failed in addition to an internationalized message that provides more human-readable details.
 */
@SuppressWarnings("serial")
public class BagException extends I18nRuntimeException {

    /**
     * Creates an exception using the supplied message.
     *
     * @param aMessage A message explaining why the exception is being thrown
     */
    public BagException(final String aMessage) {
        super(Constants.BUNDLE_NAME, normalizeWS(aMessage));
    }

    /**
     * Creates an exception using the supplied message.
     *
     * @param aMessage A message explaining why the exception is being thrown
     * @param aFile A file from which we its absolute path on the file system
     */
    public BagException(final String aMessage, final File aFile) {
        super(Constants.BUNDLE_NAME, normalizeWS(aMessage), aFile.getAbsolutePath());
    }

    /**
     * Creates an exception using the supplied message.
     *
     * @param aMessage A message explaining why the exception is being thrown
     * @param aDetail An additional detail about the cause of the exception
     */
    public BagException(final String aMessage, final String aDetail) {
        super(Constants.BUNDLE_NAME, normalizeWS(aMessage), aDetail);
    }

    /**
     * Creates an exception using the supplied message.
     *
     * @param aMessage A message explaining why the exception is being thrown
     * @param aDetailsVararg Additional details about the cause of the exception
     */
    public BagException(final String aMessage, final Object... aDetailsVararg) {
        super(Constants.BUNDLE_NAME, normalizeWS(aMessage), aDetailsVararg);
    }

    /**
     * Creates an exception using the supplied message and underlying cause.
     *
     * @param aMessage A message explaining why the exception is being thrown
     * @param aThrowable An underlying exception
     */
    public BagException(final String aMessage, final Throwable aThrowable) {
        super(Constants.BUNDLE_NAME, normalizeWS(aMessage), aThrowable);
    }

    /**
     * Cleans up whitespace issues that might arise when the XML resources file is pretty-printed; for our messages,
     * we just want straight text, no EOLs.
     *
     * @param aMessage
     * @return
     */
    private static String normalizeWS(final String aMessage) {
        return aMessage.replaceAll("\\s+", " ");
    }
}
