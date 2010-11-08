package info.freelibrary.bagit;

import info.freelibrary.util.XMLBundleControl;
import info.freelibrary.util.XMLResourceBundle;

import java.io.File;
import java.util.ResourceBundle;

/**
 * An exception thrown during the <code>Bag</code> validation process. It may
 * contain a specific reason why the validation failed in addition to an
 * internationalized message that provides more human-readable details.
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
@SuppressWarnings("serial")
public class BagException extends Exception {

	/**
	 * An exception indicating the bagit directory doesn't exist.
	 */
	public static final int BAG_DIR_DOES_NOT_EXIST = 1;

	/**
	 * An exception indicating the required data dir doesn't exist.
	 */
	public static final int DATA_DIR_DOES_NOT_EXIST = 3;

	/**
	 * An exception indicating the baginfo.txt file is invalid.
	 */
	public static final int INVALID_BAG_INFO_TXT_FILE = 5;

	/**
	 * An exception indicating the bagit.txt file isn't valid.
	 */
	public static final int INVALID_BAGIT_TXT_FILE = 11;

	/**
	 * An exception indicating that the required baginfo.txt file is missing.
	 */
	public static final int MISSING_BAG_INFO_TXT_FILE = 7;

	/**
	 * An exception indicating that the required bagit.txt file is missing.
	 */
	public static final int MISSING_BAGIT_TXT_FILE = 9;

	/**
	 * An exception that doesn't have a hard-coded reason.
	 */
	public static final int UNSPECIFIED_OTHER_REASON = 0;

	private static final XMLResourceBundle BUNDLE = (XMLResourceBundle) ResourceBundle
			.getBundle("Messages", new XMLBundleControl());

	/**
	 * Gets a textual message that corresponds to one of the hard-coded reasons.
	 * 
	 * @param aCode A hard-coded reason for the exception
	 * @return The textual message associated with the exception
	 */
	private static final String getMessageFromReason(int aCode) {
		switch (aCode) {
		case BAG_DIR_DOES_NOT_EXIST:
			return "bagit.not_found";
		case DATA_DIR_DOES_NOT_EXIST:
			return "bagit.no_data";
		case INVALID_BAG_INFO_TXT_FILE:
			return "bagit.invalid_baginfo";
		case MISSING_BAG_INFO_TXT_FILE:
			return "bagit.no_baginfo";
		case MISSING_BAGIT_TXT_FILE:
			return "bagit.no_bagit";
		case INVALID_BAGIT_TXT_FILE:
			return "bagit.invalid_bagit";
		default:
			return "bagit.other_reason";
		}
	}

	/**
	 * Hard-coded reason for the exception.
	 */
	private int myReason;

	/**
	 * Creates an exception using one of the specified reasons.
	 * 
	 * @param aReason A hard-coded reason for the exception
	 */
	public BagException(int aReason) {
		super(BUNDLE.get(getMessageFromReason(aReason)));
		myReason = aReason;
	}

	/**
	 * Creates an exception using the hard-coded reasons and underlying cause.
	 * 
	 * @param aReason A message explaining why the exception is being thrown
	 * @param aThrowable An underlying exception
	 */
	public BagException(int aReason, Throwable aThrowable) {
		super(BUNDLE.get(getMessageFromReason(aReason)), aThrowable);
		myReason = aReason;
	}
	
	/**
	 * Creates an exception using the supplied message.
	 * 
	 * @param aMessage A message explaining why the exception is being thrown
	 */
	public BagException(String aMessage) {
		super(BUNDLE.get(aMessage));
	}
	
	/**
	 * Creates an exception using the supplied message.
	 * 
	 * @param aMessage A message explaining why the exception is being thrown
	 * @param aFile A file from which we its absolute path on the file system
	 */
	public BagException(String aMessage, File aFile) {
		super(BUNDLE.get(aMessage, aFile.getAbsolutePath()));
	}
	
	/**
	 * Creates an exception using the supplied message.
	 * 
	 * @param aMessage A message explaining why the exception is being thrown
	 * @param aDetail An additional detail about the cause of the exception
	 */
	public BagException(String aMessage, String aDetail) {
		super(BUNDLE.get(aMessage, aDetail));
	}

	/**
	 * Creates an exception using the supplied message.
	 * 
	 * @param aMessage A message explaining why the exception is being thrown
	 * @param aDetailsArray Additional details about the cause of the exception
	 */
	public BagException(String aMessage, String[] aDetailsArray) {
		super(BUNDLE.get(aMessage, aDetailsArray));
	}

	/**
	 * Creates an exception using the supplied message and underlying cause.
	 * 
	 * @param aMessage A message explaining why the exception is being thrown
	 * @param aThrowable An underlying exception
	 */
	public BagException(String aMessage, Throwable aThrowable) {
		super(BUNDLE.get(aMessage), aThrowable);
	}

	/**
	 * Returns the hard-coded reason for this exception.
	 * 
	 * @return The reason for the exception
	 */
	public int getReason() {
		return myReason;
	}
}