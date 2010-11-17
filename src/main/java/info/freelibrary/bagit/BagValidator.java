package info.freelibrary.bagit;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.RegexFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagValidator extends I18nObject {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BagValidator.class);

	/**
	 * Checks whether a supplied bag is complete or not.
	 * 
	 * @param aBag A bag to check for completeness
	 * @return True if the bag is complete; else, false
	 * @throws IOException If there is trouble reading or writing the bag
	 */
	public boolean isComplete(Bag aBag) throws IOException {
		try {
			checkStructure(aBag);
		}
		catch (BagException details) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(getI18n("bagit.not_complete", new String[] {
						aBag.myDir.getName(), details.getMessage() }), details);
			}

			return false;
		}

		return true;
	}

	/**
	 * Checks whether a supplied bag is valid or not.
	 * 
	 * @param aBag A bag to check for validity
	 * @return True if the bag is valid; else, false
	 * @throws IOException If there is trouble reading or writing the bag
	 */
	public boolean isValid(Bag aBag) throws IOException {
		try {
			validate(aBag);
			return true;
		}
		catch (BagException details) {
			return false;
		}
	}

	/**
	 * Validates a bag, throwing a <code>BagException</code> if the supplied bag
	 * isn't valid.
	 * 
	 * @param aBag A bag to check for validity
	 * @return A validated bag which can be written in a variety of formats
	 * @throws BagException If the supplied bag doesn't validate
	 * @throws IOException If there is a problem reading or writing the files in
	 *         the bag
	 */
	public ValidatedBag validate(Bag aBag) throws BagException, IOException {
		checkStructure(aBag);

		// Validity is determined by whether the checksums are actually correct
		PayloadManifest payloadManifest = aBag.getManifest();
		String algorithm = payloadManifest.getHashAlgorithm();

		for (File payloadFile : payloadManifest.getFiles()) {
			try {
				String hash = payloadManifest.getStoredHash(payloadFile);
				String check = FileUtils.hash(payloadFile, algorithm);

				if (!hash.equals(check)) {
					throw new BagException(
							BagException.INVALID_PAYLOAD_CHECKSUM,
							new String[] { payloadFile.getAbsolutePath(), hash,
									check });
				}
			}
			catch (NoSuchAlgorithmException details) {
				throw new RuntimeException(details); // shouldn't see this here
			}
		}

		TagManifest tagManifest = aBag.getTagManifest();
		String tagAlgorithm = tagManifest.getHashAlgorithm();

		for (File tagFile : tagManifest.getFiles()) {
			try {
				String hash = tagManifest.getStoredHash(tagFile);
				String check = FileUtils.hash(tagFile, tagAlgorithm);

				if (!hash.equals(check)) {
					throw new BagException(BagException.INVALID_TAG_CHECKSUM,
							new String[] { tagFile.getAbsolutePath(), hash,
									check });
				}
			}
			catch (NoSuchAlgorithmException details) {
				throw new RuntimeException(details); // shouldn't see this here
			}
		}

		return new ValidatedBag(aBag);
	}

	/**
	 * Checks the structure of the bag directory to make sure it is complete.
	 * 
	 * @param aBag A bag to check for completeness
	 * @throws BagException If there is a structural problem with the bag
	 * @throws IOException If there is trouble reading or writing files in the
	 *         bag
	 */
	private void checkStructure(Bag aBag) throws BagException, IOException {
		checkRequiredFiles(aBag);
		checkPayload(aBag.getManifest(), aBag.getDataDir());
		checkTagManifest(aBag.getTagManifest(), aBag.myDir);
	}

	private void checkRequiredFiles(Bag aBag) throws BagException {
		if (!aBag.hasDeclaration()) {
			throw new BagException(BagException.MISSING_BAGIT_TXT_FILE);
		}
		
		if (aBag.getManifest().countEntries() < 1) {
			throw new BagException(BagException.MISSING_MANIFEST);
		}
	}
	
	private void checkTagManifest(TagManifest aTagManifest, File aBagDir)
			throws BagException, IOException {
		for (File tagFile : aTagManifest.getFiles()) {
			if (!tagFile.exists()) {
				throw new BagException(BagException.MISSING_TAG_FILE, tagFile
						.getAbsolutePath());
			}
		}
	}

	/**
	 * Checks the payload and payload manifest to make sure they are in sync.
	 * 
	 * @param aManifest A manifest representing the payload files
	 * @param aDataDir A bag data directory
	 * @throws BagException If there is a sync problem between manifest and
	 *         payload
	 * @throws IOException If there is trouble reading or writing the payload
	 */
	private void checkPayload(PayloadManifest aManifest, File aDataDir)
			throws BagException, IOException {
		FilenameFilter filter = new RegexFileFilter(".*");
		File[] files = FileUtils.listFiles(aDataDir, filter, true);
		File[] payloadFiles = aManifest.getFiles();

		if (files.length != payloadFiles.length) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.debug.file_count", new String[] {
						Integer.toString(files.length),
						Integer.toString(payloadFiles.length) }));
			}

			throw new BagException(
					BagException.PAYLOAD_MANIFEST_DIFFERS_FROM_DATADIR);
		}

		Arrays.sort(files);
		Arrays.sort(payloadFiles);

		for (int index = 0; index < files.length; index++) {
			String fPath = files[index].getAbsolutePath();
			String mPath = payloadFiles[index].getAbsolutePath();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.test.file_compare", new String[] {
						fPath, mPath }));
			}

			if (!fPath.equals(mPath)) {
				throw new BagException(
						BagException.PAYLOAD_MANIFEST_DIFFERS_FROM_DATADIR);
			}
		}
	}
}
