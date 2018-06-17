
package info.freelibrary.bagit;

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

import javax.xml.transform.TransformerException;

import org.apache.commons.compress.utils.Charsets;

import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.StringUtils;

/**
 * Handle for working with files in the package's data directory.
 */
public class BagData {

    static final String FILE_NAME = "data";

    private static final Logger LOGGER = LoggerFactory.getLogger(BagData.class, Constants.BUNDLE_NAME);

    private static final String PATTERN = ".*";

    private static final String SLASH = "/";

    boolean isValid;

    private final File myDataDir;

    /**
     * Creates a new <code>BagData</code> object using the data directory of an existing bag
     *
     * @param aDataDir The data directory of a bag structure
     */
    BagData(final File aDataDir) {
        if (!aDataDir.isDirectory() || !aDataDir.getName().equals(FILE_NAME)) {
            throw new I18nRuntimeException(Constants.BUNDLE_NAME, MessageCodes.BAGIT_023);
        }

        myDataDir = aDataDir;
    }

    /**
     * Returns an array of all the data file paths (relative to the bag's data directory).
     *
     * @return An array of all the data file paths, relative to the bag's data directory
     */
    public String[] getFilePaths() {
        return getFilePaths(PATTERN);
    }

    /**
     * Returns the number of files in the data directory.
     *
     * @return The number of files in the data directory
     */
    public int fileCount() {
        return getFilePaths().length;
    }

    /**
     * Returns a list of file paths (relative to the data directory) whose file extensions match those supplied in the
     * parameter strings.
     *
     * @param aExtList A file extension filter list (txt, jpg, xml, etc.)
     * @return An array of all the data file paths, relative to the bag's data directory
     */
    public String[] getFilePaths(final String... aExtList) {
        final FilenameFilter filter = new FileExtFileFilter(aExtList);
        final ArrayList<String> paths = new ArrayList<>();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_055, filter.toString());
        }

        try {
            for (final File file : FileUtils.listFiles(myDataDir, filter, true)) {
                paths.add(getRelativePath(file));
            }
        } catch (final FileNotFoundException details) {
            throw new RuntimeException(details); // should not happen
        }

        return paths.toArray(new String[paths.size()]);
    }

    /**
     * Returns an array of all the data file paths of files whose names match the supplied file name regular
     * expression; return paths are relative to the bag's data directory.
     *
     * @param aFileNameRegex A regular expression to match against the file's name
     * @return An array of all the data file paths, relative to the bag's data directory
     */
    public String[] getFilePaths(final String aFileNameRegex) {
        return getFilePaths(PATTERN, aFileNameRegex);
    }

    /**
     * Returns an array of all the matching data file paths relative to the bag's data directory.
     *
     * @param aPathRegex A regular expression to match against the pull path, minus the file name
     * @param aFileNameRegex A regular expression to match against the file's name
     * @return An array of all the data file paths, relative to the bag's data directory
     */
    public String[] getFilePaths(final String aPathRegex, final String aFileNameRegex) {
        final RegexFileFilter filter = new RegexFileFilter(aFileNameRegex);
        final ArrayList<String> paths = new ArrayList<>();

        if (LOGGER.isDebugEnabled()) {
            if (!aPathRegex.equals(PATTERN)) {
                LOGGER.debug(MessageCodes.BAGIT_054, aPathRegex);
            }

            LOGGER.debug(MessageCodes.BAGIT_053, aFileNameRegex);
        }

        try {
            for (final File file : FileUtils.listFiles(myDataDir, filter, true)) {
                final String name = file.getName();
                final String path = getRelativePath(file);
                String justPath = path.substring(0, path.lastIndexOf(name));

                if (justPath.endsWith(SLASH)) {
                    justPath = justPath.substring(0, justPath.length() - 1);
                }

                if (Pattern.matches(aPathRegex, justPath)) {
                    paths.add(path);
                }
            }
        } catch (final FileNotFoundException details) {
            throw new RuntimeException(details); // should not happen
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_052, Integer.toString(paths.size()));
        }

        return paths.toArray(new String[paths.size()]);
    }

    /**
     * Outputs an XML representation of the bag's data directory, useful for debugging.
     *
     * @return An XML representation of the bag's data directory
     */
    @Override
    public String toString() {
        final String systemPath = myDataDir.getAbsolutePath();
        String dataDirXML;

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_035, myDataDir);
            }

            dataDirXML = FileUtils.toXML(myDataDir.getAbsolutePath(), true);
            dataDirXML = dataDirXML.replace(systemPath + SLASH, "");
        } catch (final TransformerException details) {
            throw new RuntimeException(details);
        } catch (final FileNotFoundException details) {
            throw new RuntimeException(details);
        }

        return dataDirXML;
    }

    /**
     * An <code>InputStream</code> from which the contents of the file of the supplied path can be read. The stream is
     * just a raw stream that you might want to wrap with an <code>InputStreamReader</code> (for encoding) and/or
     * <code>BufferedReader</code>.
     *
     * @param aPath The path of the file from which we want to read
     * @return A character reader for the file represented by the supplied path
     * @throws FileNotFoundException If the file for the supplied path cannot be found or it is a directory
     */
    public InputStream getInputStream(final String aPath) throws FileNotFoundException {
        final File file = new File(myDataDir, aPath);

        if (file.exists() && file.isFile()) {
            return new FileInputStream(file);
        } else {
            throw new FileNotFoundException(aPath);
        }
    }

    /**
     * A character <code>Reader</code> from which the contents of the file of the supplied path can be read. The
     * reader is a <code>FileReader</code> that you might want to wrap with a <code>BufferedReader</code>.
     *
     * @param aPath The path of the file from which we want to read
     * @return A character reader for the file represented by the supplied path
     * @throws FileNotFoundException If the file for the supplied path cannot be found or it is a directory
     */
    public Reader getReader(final String aPath) throws FileNotFoundException {
        final File file = new File(myDataDir, aPath);

        if (file.exists() && file.isFile()) {
            return new FileReader(file);
        } else {
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
    public long getSize(final String aPath) throws FileNotFoundException {
        final File file = new File(myDataDir, aPath);

        if (file.exists()) {
            return file.length();
        } else {
            throw new FileNotFoundException(aPath);
        }
    }

    /**
     * Reads the supplied file path into a string. The system determines the encoding.
     *
     * @param aPath A path to a file in the bag's payload
     * @return A string with the contents from the supplied path's file
     * @throws FileNotFoundException If the supplied file path cannot be found or it is a directory
     * @throws IOException If there is trouble reading the file of the supplied path
     */
    public String getAsString(final String aPath) throws FileNotFoundException, IOException {
        final File file = new File(myDataDir, aPath);

        if (file.exists() && file.isFile()) {
            return StringUtils.read(file);
        } else {
            throw new FileNotFoundException(aPath);
        }
    }

    /**
     * Reads the supplied file path into a UTF-8 string.
     *
     * @param aPath A path to a file in the bag's payload
     * @return A UTF-8 string with the contents from the supplied path's file
     * @throws FileNotFoundException If the supplied file path cannot be found or it is a directory
     * @throws IOException If there is trouble reading the file of the supplied path
     */
    public String getAsUTF8String(final String aPath) throws FileNotFoundException, IOException {
        final File file = new File(myDataDir, aPath);

        if (file.exists() && file.isFile()) {
            return StringUtils.read(file, Charsets.UTF_8);
        } else {
            throw new FileNotFoundException(aPath);
        }
    }

    /**
     * Remove a path (file or directory) from the bag's payload.
     *
     * @param aPath A path (file or directory) to remove from the payload
     */
    public void removeFile(final String aPath) {
        final File file = new File(myDataDir, aPath);

        if (file.exists()) {
            FileUtils.delete(file);
        }
    }

    /**
     * Gets the path of the supplied <code>File</code>, relative to the bag's data directory.
     *
     * @param aFile A file from which to get the relative path
     */
    private String getRelativePath(final File aFile) {
        final int start = myDataDir.getAbsolutePath().length() + 1;
        return aFile.getAbsolutePath().substring(start);
    }
}
