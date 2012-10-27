package info.freelibrary.bagit;

import info.freelibrary.util.DOMUtils;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

/**
 * Package structure that encapsulates descriptive tags and a payload.
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
public class Bag extends I18nObject {

	private static final Logger LOGGER = LoggerFactory.getLogger(Bag.class);

	static final String WORK_DIR = "bagit.workdir";

	private boolean isValid;

	private BagInfo myBagInfo;

	private BagData myBagData;

	private boolean myBagIsOverwritten;

	private Declaration myDeclaration;

	private PayloadManifest myManifest;

	private TagManifest myTagManifest;

	File myDir;

	/**
	 * Creates a new package from scratch or from an existing bag.
	 * 
	 * @param aBag A bag (either a bag directory or tar, tar.bz, zip, or tar.gz
	 *        file)
	 * @throws IOException An exception indicating there was problem reading or
	 *         writing the bag
	 */
	public Bag(File aBag) throws IOException {
		this(aBag, false);
	}

	/**
	 * Creates a new package from scratch or from an existing bag. The overwrite
	 * option indicates whether an existing bad directory should be changed in
	 * place or not. An overwrite value of &quot;true&quot; only makes sense for
	 * bag directories; it is ignored for tar, zip, tar.gz, and tar.bz2 bags.
	 * 
	 * @param aBag A <code>Bag</code> in file or directory form
	 * @param aOverwrite A boolean indicating whether an existing directory
	 *        should be overwritten
	 * @throws IOException An exception indicating there was problem reading or
	 *         writing the bag
	 */
	public Bag(File aBag, boolean aOverwrite) throws IOException {
		myBagIsOverwritten = aOverwrite;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.creating", aBag.getName()));
		}

		// If we're using an existing bag, copy its contents into our dir
		if (aBag.exists()) {
			if (aBag.isDirectory()) {
				myDir = !aOverwrite ? createWorkingDir(aBag) : aBag;

				// We want to work on a copy rather than the original directory
				if (!aOverwrite && !myDir.mkdirs()) {
					throw new IOException(getI18n("bagit.dir_create", myDir));
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(getI18n("bagit.debug.existing"));
				}

				if (!aOverwrite) {
					FileUtils.copy(aBag, myDir);
				}
			}
			else {
				String mimeDetector = MagicMimeMimeDetector.class.getName();
				MimeUtil.registerMimeDetector(mimeDetector);
				Collection<?> types = MimeUtil.getMimeTypes(aBag);
				MimeType type = MimeUtil.getMostSpecificMimeType(types);
				String mimeType = type.toString();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(getI18n("bagit.debug.mime", new String[] {
							aBag.getName(), mimeType }));
				}

				if (mimeType.equals("application/x-gzip")) {
					myDir = BagPackager.fromTarGz(aBag).myDir;
					myBagIsOverwritten = true;
				}
				else if (mimeType.equals("application/x-tar")) {
					myDir = BagPackager.fromTar(aBag).myDir;
					myBagIsOverwritten = true;
				}
				else if (mimeType.equals("application/zip")) {
					myDir = BagPackager.fromZip(aBag).myDir;
					myBagIsOverwritten = true;
				}
				else if (mimeType.equals("application/x-bzip2")) {
					myDir = BagPackager.fromTarBZip2(aBag).myDir;
					myBagIsOverwritten = true;
				}
				else {
					try {
						clean();
					}
					catch (Throwable throwable) {
						LOGGER.warn(throwable.getMessage());
					}

					throw new IOException(getI18n("bagit.unknown_mime_type",
							new String[] { aBag.getAbsolutePath(), mimeType }));
				}
			}

			setBagInfo(new BagInfo(myDir));

			try {
				myDeclaration = new Declaration(myDir);
			}
			catch (FileNotFoundException details) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(details.getMessage());
				}
			}
		}
		else {
			myDir = !aOverwrite ? createWorkingDir(aBag) : aBag;

			// We want to work on a copy rather than the original directory
			if (!aOverwrite && !myDir.mkdirs()) {
				throw new IOException(getI18n("bagit.dir_create", myDir));
			}

			myDeclaration = new Declaration(myDir, false);
			myDeclaration.writeToFile();
		}

		myManifest = new PayloadManifest(myDir);
		myTagManifest = new TagManifest(myDir);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.data_dir"));
		}

		File dataDir = new File(myDir, BagData.FILE_NAME);

		if (!dataDir.exists() && !dataDir.mkdir()) {
			try {
				if (!aOverwrite) {
					clean();
				}
			}
			catch (Throwable throwable) {
				LOGGER.warn(throwable.getMessage());
			}

			throw new IOException(getI18n("bagit.dir_create", dataDir));
		}

		// Add a cleanup thread to catch whatever isn't caught by finalize
		// This has small footprint, doesn't use many resources until it's run
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(getI18n("bagit.debug.shutdown_hook", myDir));
				}

				clean();
			}
		});
	}

	/**
	 * Creates a new package from scratch or from an existing bag.
	 * 
	 * @param aBagName The name of a bag (either a bag directory, new or
	 *        existing, or a tar, tar.bz, zip, or tar.gz file)
	 * @throws IOException An exception indicating there was problem reading or
	 *         writing the bag
	 */
	public Bag(String aBagName) throws IOException {
		this(new File(aBagName));
	}

	/**
	 * Creates a new package from scratch or from an existing bag. The overwrite
	 * option indicates whether an existing bad directory should be changed in
	 * place or not. An overwrite value of &quot;true&quot; only makes sense for
	 * bag directories; it is ignored for tar, zip, tar.gz, and tar.bz2 bags.
	 * 
	 * @param aBagName The name of a bag (either a bag directory, new or
	 *        existing, or a tar, tar.bz, zip, or tar.gz file)
	 * @throws IOException An exception indicating there was problem reading or
	 *         writing the bag
	 */
	public Bag(String aBagName, boolean aOverwrite) throws IOException {
		this(new File(aBagName), aOverwrite);
	}

	/**
	 * Creates a new package from scratch or from an existing bag and populates
	 * the bag-info.txt with values from the supplied <code>Properties</code>.
	 * 
	 * @param aBagName The name of a bag (either a bag directory, new or
	 *        existing, or a tar, tar.bz, zip, or tar.gz file)
	 * @param aProperties Metadata values to be added to the bag-info.txt
	 * @throws IOException An exception indicating there was problem reading or
	 *         writing the bag
	 */
	public Bag(String aBagName, Properties aProperties) throws IOException {
		this(new File(aBagName));
		setBagInfo(new BagInfo(aProperties));
	}

	/**
	 * Creates a new package from scratch or from an existing bag and populates
	 * the bag-info.txt with values from the supplied <code>Properties</code>.
	 * 
	 * @param aBag A bag (either a bag directory, new or existing, or a tar,
	 *        tar.bz, zip, or tar.gz file)
	 * @param aProperties Metadata values to be added to the bag-info.txt
	 * @throws IOException An exception indicating there was problem reading or
	 *         writing the bag
	 */
	public Bag(File aBag, Properties aProperties) throws IOException {
		this(aBag);
		setBagInfo(new BagInfo(aProperties));
	}

	/**
	 * Gets a representation of the bag's payload. However, files can be added
	 * to the bag by using <code>addData(File)</code> without having to get the
	 * <code>BagData</code> object.
	 * 
	 * @return
	 */
	public BagData getBagData() {
		if (myBagData == null) {
			myBagData = new BagData(new File(myDir, BagData.FILE_NAME));
		}

		return myBagData;
	}

	/**
	 * Add files and directories to the bag's payload.
	 * 
	 * @param aFiles File to add to the bag payload.
	 * @throws IOException If there is difficulty writing the new files
	 * @throws RuntimeException If the bag to which we're adding files has
	 *         already been validated
	 */
	public void addData(File... aFiles) throws IOException, RuntimeException {
		File dataDir = new File(myDir, BagData.FILE_NAME);

		if (isValid) {
			throw new RuntimeException(getI18n("bagit.validated"));
		}

		for (File fromFile : aFiles) {
			File toFile = new File(dataDir, fromFile.getName());

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("bagit.debug.add_data", fromFile.getName());
			}

			FileUtils.copy(fromFile, toFile);
			myManifest.updateWith(toFile);
		}
	}

	/**
	 * Completes the bag structure.
	 * 
	 * @throws IOException If there is difficulty writing the missing files
	 */
	public Bag complete() throws IOException {
		File dataDir = new File(myDir, BagData.FILE_NAME);

		if (!dataDir.exists() && !dataDir.mkdirs()) {
			throw new IOException(getI18n("bagit.dir_create", dataDir));
		}

		if (!hasDeclaration()) {
			myDeclaration = new Declaration(myDir, false);
			myDeclaration.writeToFile();
		}

		getManifest().writeToFile();

		return this;
	}

	/**
	 * Returns the <code>BagInfo</code> for this bag.
	 * 
	 * @return The <code>BagInfo</code> for this bag
	 */
	public BagInfo getBagInfo() {
		if (myBagInfo == null) {
			myBagInfo = new BagInfo();
		}

		return myBagInfo;
	}

	/**
	 * Saves to the file system in the form of a bag directory.
	 * 
	 * @return A directory representing this bag package
	 * @throws IOException If there is a problem writing the bag to the file
	 *         system
	 */
	public File toDir() throws IOException {
		if (myBagIsOverwritten) {
			return myDir;
		}
		else {
			File dir = myDir.getParentFile();
			String name = myDir.getName();
			int end = name.lastIndexOf('_');
			File bagDir = new File(dir.getParentFile(), name.substring(0, end));

			if (bagDir.exists()) {
				FileUtils.delete(bagDir);
			}

			FileUtils.copy(myDir, bagDir);

			return bagDir;
		}

		// TODO write a unit test to make sure this does what I think
	}

	/**
	 * Sets the <code>BagInfo</code> for this bag.
	 * 
	 * @param aBagInfo The <code>BagInfo</code> for this bag
	 * @throws RuntimeException If the bag to which the <code>BagInfo</code> is
	 *         being added has already been validated
	 */
	public void setBagInfo(BagInfo aBagInfo) throws RuntimeException {
		if (isValid) {
			throw new RuntimeException(getI18n("bagit.validated"));
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.setting_info"));
		}

		myBagInfo = aBagInfo;
	}

	/**
	 * Gets the &quot;octetstream sum&quot; of the payload, namely, a two- part
	 * number of the form "OctetCount.StreamCount", where OctetCount is the
	 * total number of octets (8-bit bytes) across all payload file content and
	 * StreamCount is the total number of payload files.
	 * 
	 * @return The &quot;octetstream sum&quot; of the payload
	 */
	public String getPayloadOxum() {
		BagData bagData = getBagData();
		long bytes = 0;
		int count = 0;

		for (String fileName : bagData.getFilePaths()) {
			try {
				long size = bagData.getSize(fileName);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(getI18n("bagit.debug.oxum_file"), size,
							fileName);
				}

				bytes += size;
				count += 1;
			}
			catch (FileNotFoundException details) {
				throw new RuntimeException(details); // shouldn't be possible
			}
		}

		return bytes + "." + count;
	}

	/**
	 * Returns the size of the bag in bytes.
	 * 
	 * @return The size of the bag in bytes
	 */
	public long getSize() {
		return FileUtils.getSize(myDir);
	}

	/**
	 * Returns an XML representation of the bag.
	 * 
	 * @return An XML representation of the bag
	 */
	public String toString() {
		String eol = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();

		DOMUtils.brokenUp(true); // pretty-print(ish) our XML

		builder.append("<bag name=\"").append(myDir.getName()).append("\">");
		builder.append(eol);

		try {
			builder.append(getDeclaration().toString());
		}
		catch (IOException details) {
			throw new RuntimeException(details);
		}

		builder.append(getBagInfo().toString());
		builder.append(getManifest().toString());
		builder.append(getTagManifest().toString());
		builder.append(getBagData().toString());
		builder.append("</bag>");

		return builder.toString();
	}

	/**
	 * Cleans up all the temporary files created during the manipulation of the
	 * bag.
	 */
	private void clean() {
		String clean = System.getProperty("bagit.autoclean", "true");

		if (clean.equals("true")) {
			File workDir = myBagIsOverwritten ? myDir : myDir.getParentFile();

			if (!workDir.exists()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(getI18n("bagit.debug.cleaned_up", workDir));
				}

				return;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.cleanup", workDir));
			}

			if (!FileUtils.delete(workDir)) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(getI18n("bagit.cleanup_failed"));
				}
			}
		}
	}

	private File createWorkingDir(File aBagDir) {
		String workDirPath = System.getProperty(Bag.WORK_DIR);
		String fileName = aBagDir.getName() + "_" + new Date().getTime();
		File workDir;

		// TODO: check system space to confirm we'll have space to continue

		// If we have a work directory, use that
		if (workDirPath != null) {
			workDir = new File(workDirPath);
		}
		else {
			workDir = aBagDir.getParentFile();
		}

		// Create a temporary working directory for our bag
		File tmpBagDir = new File(workDir, fileName);
		return new File(tmpBagDir, aBagDir.getName());
	}

	Declaration getDeclaration() throws IOException {
		if (myDeclaration == null) {
			myDeclaration = new Declaration(myDir, false);
		}

		return myDeclaration;
	}

	protected void finalize() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.finalizing", myDir));
		}

		clean();
	}

	PayloadManifest getManifest() {
		if (myManifest == null) {
			try {
				myManifest = new PayloadManifest(myDir);
			}
			catch (IOException details) {
				throw new RuntimeException(details); // shouldn't happen
			}
		}

		return myManifest;
	}

	TagManifest getTagManifest() {
		if (myTagManifest == null) {
			try {
				myTagManifest = new TagManifest(myDir);
			}
			catch (IOException details) {
				throw new RuntimeException(details); // shouldn't happen
			}
		}

		return myTagManifest;
	}

	boolean hasDeclaration() {
		return myDeclaration != null;
	}

	void validate() {
		isValid = true;
		myBagData.isValid = true;
		myBagInfo.isValid = true;
	}
}
