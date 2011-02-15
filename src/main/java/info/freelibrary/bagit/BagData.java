package info.freelibrary.bagit;

import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagData extends I18nObject {

	private static final Logger LOGGER = LoggerFactory.getLogger(BagData.class);

	static final String FILE_NAME = "data";
	
	private File myDataDir;

	boolean isValid;

	/**
	 * Creates a new <code>BagData</code> object using the data directory of an
	 * existing bag
	 * 
	 * @param aDataDir The data directory of a bag structure
	 */
	BagData(File aDataDir) {
		if (!aDataDir.isDirectory() || !aDataDir.getName().equals(FILE_NAME)) {
			throw new RuntimeException(getI18n("bagit.data_dir"));
		}

		myDataDir = aDataDir;
	}

	/**
	 * Returns an array of all the data file paths (relative to the bag's data
	 * directory).
	 * 
	 * @return An array of all the data file paths, relative to the bag's data
	 *         directory
	 */
	public String[] getFilePaths() {
		return getFilePaths(".*");
	}

	/**
	 * Returns a list of file paths (relative to the data directory) whose file
	 * extensions match those supplied in the parameter strings.
	 * 
	 * @param aExtList A file extension filter list (txt, jpg, xml, etc.)
	 * @return An array of all the data file paths, relative to the bag's data
	 *         directory
	 */
	public String[] getFilePaths(String... aExtList) {
		FilenameFilter filter = new FileExtFileFilter(aExtList);
		ArrayList<String> paths = new ArrayList<String>();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.testing_exts", filter.toString()));
		}

		try {
			for (File file : FileUtils.listFiles(myDataDir, filter, true)) {
				paths.add(getRelativePath(file));
			}
		}
		catch (FileNotFoundException details) {
			throw new RuntimeException(details); // should not happen
		}

		return paths.toArray(new String[paths.size()]);
	}

	/**
	 * Returns an array of all the data file paths of files whose names match
	 * the supplied file name regular expression; return paths are relative to
	 * the bag's data directory.
	 * 
	 * @param aFileNameRegex A regular expression to match against the file's
	 *        name
	 * @return An array of all the data file paths, relative to the bag's data
	 *         directory
	 */
	public String[] getFilePaths(String aFileNameRegex) {
		return getFilePaths(".*", aFileNameRegex);
	}

	/**
	 * Returns an array of all the matching data file paths relative to the
	 * bag's data directory.
	 * 
	 * @param aPathRegex A regular expression to match against the pull path,
	 *        minus the file name
	 * @param aFileNameRegex A regular expression to match against the file's
	 *        name
	 * @return An array of all the data file paths, relative to the bag's data
	 *         directory
	 */
	public String[] getFilePaths(String aPathRegex, String aFileNameRegex) {
		RegexFileFilter filter = new RegexFileFilter(aFileNameRegex);
		ArrayList<String> paths = new ArrayList<String>();

		if (LOGGER.isDebugEnabled()) {
			if (!aPathRegex.equals(".*")) {
				LOGGER.debug(getI18n("bagit.debug.testing_path", aPathRegex));
			}

			LOGGER.debug(getI18n("bagit.debug.testing_file", aFileNameRegex));
		}

		try {
			for (File file : FileUtils.listFiles(myDataDir, filter, true)) {
				String name = file.getName();
				String path = getRelativePath(file);
				String justPath = path.substring(0, path.lastIndexOf(name));

				if (justPath.endsWith("/")) {
					justPath = justPath.substring(0, justPath.length() - 1);
				}

				if (Pattern.matches(aPathRegex, justPath)) {
					paths.add(path);
				}
			}
		}
		catch (FileNotFoundException details) {
			throw new RuntimeException(details); // should not happen
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getI18n("bagit.debug.paths_found",
					Integer.toString(paths.size())));
		}

		return paths.toArray(new String[paths.size()]);
	}

	/**
	 * Outputs an XML representation of the bag's data directory, useful for
	 * debugging.
	 * 
	 * @return An XML representation of the bag's data directory
	 */
	public String toString() {
		String systemPath = myDataDir.getAbsolutePath();
		String dataDirXML;

		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.debug.reading", myDataDir));
			}

			dataDirXML = FileUtils.toXML(myDataDir.getAbsolutePath(), true);
			dataDirXML = dataDirXML.replace(systemPath + "/", "");
		}
		catch (ParserConfigurationException details) {
			throw new RuntimeException(details);
		}
		catch (FileNotFoundException details) {
			throw new RuntimeException(details);
		}

		return dataDirXML;
	}

	/**
	 * An <code>InputStream</code> from which the contents of the file of
	 * the supplied path can be read.
	 * 
	 * @param aPath The path of the file from which we want to read
	 * @return A character reader for the file represented by the supplied path
	 * @throws FileNotFoundException If the file for the supplied path cannot be
	 *         found or it is a directory
	 */
	public InputStream getInputStream(String aPath)
			throws FileNotFoundException {
		File file = new File(myDataDir, aPath);

		if (file.exists() && file.isFile()) {
			return new FileInputStream(file);
		}
		else {
			throw new FileNotFoundException(aPath);
		}
	}

	/**
	 * A character <code>Reader</code> from which the contents of the file of
	 * the supplied path can be read.
	 * 
	 * @param aPath The path of the file from which we want to read
	 * @return A character reader for the file represented by the supplied path
	 * @throws FileNotFoundException If the file for the supplied path cannot be
	 *         found or it is a directory
	 */
	public Reader getReader(String aPath) throws FileNotFoundException {
		File file = new File(myDataDir, aPath);

		if (file.exists() && file.isFile()) {
			return new FileReader(file);
		}
		else {
			throw new FileNotFoundException(aPath);
		}
	}

	/**
	 * Returns the byte size of the file corresponding to the supplied file path.
	 * 
	 * @param aPath The path, relative to the bag's data directory, of the desired file
	 * @return The size, in bytes, of the requested file
	 * @throws FileNotFoundException If the supplied path doesn't correspond to a file
	 */
	public long getSize(String aPath) throws FileNotFoundException {
		File file = new File(myDataDir, aPath);
		
		if (file.exists()) {
			return file.length();
		}
		else {
			throw new FileNotFoundException(aPath);
		}
	}
	
	/**
	 * Reads the supplied file path into a string. The system determines the
	 * encoding.
	 * 
	 * @param aPath A path to a file in the bag's payload
	 * @return A string with the contents from the supplied path's file
	 * @throws FileNotFoundException If the supplied file path cannot be found
	 *         or it is a directory
	 * @throws IOException If there is trouble reading the file of the supplied
	 *         path
	 */
	public String getAsString(String aPath) throws FileNotFoundException,
			IOException {
		File file = new File(myDataDir, aPath);

		if (file.exists() && file.isFile()) {
			return StringUtils.read(file);
		}
		else {
			throw new FileNotFoundException(aPath);
		}
	}

	/**
	 * Reads the supplied file path into a UTF-8 string.
	 * 
	 * @param aPath A path to a file in the bag's payload
	 * @return A UTF-8 string with the contents from the supplied path's file
	 * @throws FileNotFoundException If the supplied file path cannot be found
	 *         or it is a directory
	 * @throws IOException If there is trouble reading the file of the supplied
	 *         path
	 */
	public String getAsUTF8String(String aPath) throws FileNotFoundException,
			IOException {
		File file = new File(myDataDir, aPath);

		if (file.exists() && file.isFile()) {
			return StringUtils.readAsUTF8(file);
		}
		else {
			throw new FileNotFoundException(aPath);
		}
	}

	/**
	 * Remove a path (file or directory) from the bag's payload.
	 * 
	 * @param aPath A path (file or directory) to remove from the payload
	 */
	public void removeFile(String aPath) {
		if (aPath != null && aPath.length() > 0) {
			FileUtils.delete(new File(myDataDir, aPath));
		}
		else {
			throw new NullPointerException();
		}
	}
	
	/**
	 * Gets the path of the supplied <code>File</code>, relative to the bag's
	 * data directory.
	 * 
	 * @param aFile A file from which to get the relative path
	 */
	private String getRelativePath(File aFile) {
		int start = myDataDir.getAbsolutePath().length() + 1;
		return aFile.getAbsolutePath().substring(start);
	}
}
