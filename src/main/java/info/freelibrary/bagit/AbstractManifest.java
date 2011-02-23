package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.util.BufferedFileReader;
import info.freelibrary.util.BufferedFileWriter;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.StringUtils;

/**
 * An abstract manifest class from which other specific manifests are
 * implemented.
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
abstract class AbstractManifest extends I18nObject {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractManifest.class);

	private static class Entry implements Comparable<Entry> {
		private File myFile;
		private String myHash;

		private Entry(File aFile, String aHash) {
			myFile = aFile;
			myHash = aHash;
		}

		@Override
		public int compareTo(Entry aEntry) {
			if (aEntry == this) {
				return 0;
			}

			return myFile.getAbsolutePath().compareToIgnoreCase(
					aEntry.myFile.getAbsolutePath());
		}

		public boolean equals(Object aObject) {
			if (aObject == this)
				return true;

			if (aObject instanceof Entry) {
				return myFile.getAbsolutePath().equals(
						((Entry) aObject).myFile.getAbsolutePath());
			}

			return false;
		}

		public int hashCode() {
			return myFile.getAbsolutePath().hashCode();
		}
	}

	private static final String DEFAULT_ALGORITHM = "md5";

	private static final String HASH_PROP = "bagit.hash";

	private File myBagDir;

	private String myFileName;

	private String myHashAlgorithm;

	private ArrayList<Entry> myHashes;

	private boolean myManifestsAreSortable;

	protected AbstractManifest(File aBagDir) throws IOException {
		String pattern = StringUtils.formatMessage(getNamePattern(), "(.*)");
		File[] files = aBagDir.listFiles(new RegexFileFilter(pattern));

		myHashes = new ArrayList<Entry>();
		myBagDir = aBagDir;

		// Bag can contain more than one manifest, but must not contain more
		// than one of the same algorithm type - TODO: handle 2+ algorithms?
		if (files.length > 0) {
			String fileName = files[0].getName();
			Matcher matcher = Pattern.compile(pattern).matcher(fileName);
			BufferedFileReader reader = null;
			String line;

			try {
				reader = new BufferedFileReader(files[0]);

				if (getLogger().isDebugEnabled()) {
					getLogger()
							.debug("Found existing tagmanifest: " + fileName);
				}

				matcher.find();
				myHashAlgorithm = matcher.group(1);

				if (getLogger().isDebugEnabled()) {
					getLogger().debug(
							"Using hash algorithm: " + myHashAlgorithm);
				}

				if (getLogger().isDebugEnabled()) {
					getLogger().debug(
							getI18n("bagit.debug.reading_manifest", files[0]));
				}

				while ((line = reader.readLine()) != null) {
					String cleanedLine = line.replaceAll("[\\s|\\*]+", " ");
					String[] parts = cleanedLine.split(" ");
					File file = new File(aBagDir, parts[1]);
					String relativePath = getRelativePath(file);

					if (getLogger().isDebugEnabled()) {
						String value = parts[0] + " " + relativePath;
						String message = getI18n("bagit.debug.reading", value);
						getLogger().debug(message);
					}

					myHashes.add(new Entry(file, parts[0]));
				}
			}
			finally {
				reader.close();
			}
		}
		else {
			pattern = getNamePattern();
			myHashAlgorithm = System.getProperty(HASH_PROP, DEFAULT_ALGORITHM);
			myFileName = StringUtils.formatMessage(pattern, myHashAlgorithm);

			if (getLogger().isDebugEnabled()) {
				getLogger().debug("Using hash algorithm: " + myHashAlgorithm);
			}

			myManifestsAreSortable = true;
		}
	}

	/**
	 * Returns the hash algorithm used in the manifest file.
	 * 
	 * @return The hash algorithm in the manifest file
	 */
	public String getHashAlgorithm() {
		return myHashAlgorithm;
	}

	/**
	 * Returns the number of entries in the manifest file.
	 * 
	 * @return The number of entries in the manifest file
	 */
	public int countEntries() {
		return myHashes.size();
	}

	public String toString() {
		String eol = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();
		Iterator<Entry> iterator = myHashes.iterator();

		while (iterator.hasNext()) {
			Entry entry = iterator.next();

			builder.append("<file name=\"");
			builder.append(getRelativePath(entry.myFile)).append("\" hash=\"");
			builder.append(entry.myHash).append("\" algorithm=\"");
			builder.append(myHashAlgorithm).append("\" />").append(eol);
		}

		return builder.toString();
	}

	/**
	 * Gets the path of the supplied <code>File</code>, relative to the BagIt
	 * working directory.
	 * 
	 * @param aFile A file to get the relative path of (relative to the working
	 *        directory)
	 */
	private String getRelativePath(File aFile) {
		int start = myBagDir.getAbsolutePath().length() + 1;
		return aFile.getAbsolutePath().substring(start);
	}

	protected abstract Logger getLogger();

	protected abstract String getNamePattern();

	File[] getFiles() {
		ArrayList<File> files = new ArrayList<File>();
		Iterator<Entry> entryIterator = myHashes.iterator();

		while (entryIterator.hasNext()) {
			Entry entry = entryIterator.next();
			files.add(entry.myFile);
		}

		return files.toArray(new File[myHashes.size()]);
	}

	/**
	 * Returns the hash associated with the supplied <code>File</code> or null
	 * if there isn't one yet.
	 * 
	 * @param aFile
	 * @return
	 */
	String getStoredHash(File aFile) {
		Iterator<Entry> entryIterator = myHashes.iterator();

		while (entryIterator.hasNext()) {
			Entry entry = entryIterator.next();

			if (entry.myFile.getAbsolutePath().equals(aFile.getAbsolutePath())) {
				return entry.myHash;
			}
		}

		return null;
	}

	/**
	 * Adds a new file to the manifest.
	 * 
	 * @param aFile
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	void add(File aFile) throws IOException, NoSuchAlgorithmException {
		myHashes.add(new Entry(aFile, FileUtils.hash(aFile, myHashAlgorithm)));
	}

	/**
	 * Deletes the manifest file(s).
	 * 
	 * @throws IOException If there is trouble deleting the manifest file(s)
	 */
	void delete() throws IOException {
		String pattern = StringUtils.formatMessage(getNamePattern(), "[.*]");
		RegexFileFilter mFilter = new RegexFileFilter(pattern);

		// Remove the old payload file manifest if it exists
		for (File manifest : myBagDir.listFiles(mFilter)) {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug(getI18n("bagit.debug.manifest", manifest));
			}

			if (!manifest.delete()) {
				throw new IOException(getI18n("bagit.file_delete", manifest));
			}
		}

		myHashes.clear();
	}

	boolean remove(File aFile) throws IOException, NoSuchAlgorithmException {
		String algorithm = FileUtils.hash(aFile, myHashAlgorithm);
		return myHashes.remove(new Entry(aFile, algorithm));
	}

	/**
	 * Writes the contents of the manifest to a file in the BagIt working
	 * directory.
	 * 
	 * @throws IOException If there is a problem writing the manifest file
	 */
	void writeToFile() throws IOException {
		File manifestFile = new File(myBagDir, myFileName);
		BufferedFileWriter writer = null;
		Iterator<Entry> iterator;

		if (myManifestsAreSortable) {
			Collections.sort(myHashes);
		}

		iterator = myHashes.iterator();

		try {
			if (manifestFile.exists()) {
				manifestFile.delete();
			}

			writer = new BufferedFileWriter(manifestFile);

			while (iterator.hasNext()) {
				Entry entry = iterator.next();

				writer.write(entry.myHash);
				writer.write(" ");
				writer.write(getRelativePath(entry.myFile));
				writer.newLine();
			}
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
