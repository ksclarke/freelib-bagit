package info.freelibrary.bagit;

import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.RegexFileFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagData extends I18nObject {

	private static final Logger LOGGER = LoggerFactory.getLogger(BagData.class);

	private File myDataDir;

	boolean isValid;

	/**
	 * Creates a new <code>BagData</code> object using the data directory of an
	 * existing bag
	 * 
	 * @param aDataDir The data directory of a bag structure
	 */
	BagData(File aDataDir) {
		if (!aDataDir.isDirectory() || !aDataDir.getName().equals("data")) {
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
