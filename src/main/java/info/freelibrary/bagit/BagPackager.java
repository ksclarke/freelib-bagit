
package info.freelibrary.bagit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;
import org.xeustechnologies.jtar.TarOutputStream;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.RegexFileFilter;

/**
 * A bag packager that prepares bags for transmission.
 */
final class BagPackager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BagPackager.class, Constants.BUNDLE_NAME);

    private static final String PATTERN = ".*";

    private static final String TAR_EXT = ".tar";

    private static final String ZIP_EXT = ".zip";

    private BagPackager() {
        super();
    }

    static File toTarBZip2(final Bag aBag) throws FileNotFoundException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_039, aBag.myDir);
        }

        final File newFile = getNewFile(aBag.myDir);
        final File parent = newFile.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, parent));
        }

        final File tarFile = toTar(aBag);
        final String fileName = newFile.getName() + ".tar.bz2";
        final File tarBz2File = new File(parent, fileName);

        BZip2CompressorOutputStream bz2Stream = null;
        FileOutputStream outStream = null;
        FileInputStream inStream = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_044, tarFile);
        }

        try {
            outStream = new FileOutputStream(tarBz2File);
            inStream = new FileInputStream(tarFile);
            bz2Stream = new BZip2CompressorOutputStream(outStream);

            IOUtils.copyStream(inStream, bz2Stream);
        } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(bz2Stream);
        }

        if (!tarFile.delete()) {
            LOGGER.debug(MessageCodes.BAGIT_045, tarFile);
        }

        return tarBz2File;
    }

    static Bag fromTarBZip2(final File aBZip2File) throws FileNotFoundException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_038, aBZip2File);
        }

        final File newFile = getNewFile(aBZip2File);
        final File parent = newFile.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, parent));
        }

        final String fileName = newFile.getName() + TAR_EXT;
        final File tarFile = new File(newFile.getParentFile(), fileName);
        final FileInputStream fileInputStream = new FileInputStream(aBZip2File);
        final BZip2CompressorInputStream bz2Stream = new BZip2CompressorInputStream(fileInputStream);
        final FileOutputStream fileOutputStream = new FileOutputStream(tarFile);
        IOUtils.copyStream(bz2Stream, fileOutputStream);
        final Bag bag = fromTar(tarFile);

        if (!tarFile.delete() && LOGGER.isWarnEnabled()) {
            LOGGER.warn(MessageCodes.BAGIT_045, tarFile);
        }

        return bag;
    }

    static File toZip(final Bag aBag) throws FileNotFoundException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_041, aBag.myDir);
        }

        final File newFile = getNewFile(aBag.myDir);
        final File parent = newFile.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, parent));
        }

        final String fileName = newFile.getName() + ZIP_EXT;
        final File zipFile = new File(parent, fileName);
        final FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
        final ZipOutputStream zipStream = new ZipOutputStream(fileOutputStream);
        final FilenameFilter filter = new RegexFileFilter(PATTERN);
        final File[] files = FileUtils.listFiles(aBag.myDir, filter, true);

        for (final File file : files) {
            final String entryName = getEntryName(aBag.myDir, file);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_047, entryName);
            }

            zipStream.putNextEntry(new ZipEntry(entryName));

            final FileInputStream inputStream = new FileInputStream(file);

            try {
                IOUtils.copyStream(inputStream, zipStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        IOUtils.closeQuietly(zipStream);

        return zipFile;
    }

    static Bag fromZip(final File aZipFile) throws FileNotFoundException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_040, aZipFile);
        }

        final File bagDir = getNewFile(aZipFile);
        final File workDir = bagDir.getParentFile(); // get parent b/c zip has dir
        final FileInputStream fileInputStream = new FileInputStream(aZipFile);
        final ZipInputStream zipStream = new ZipInputStream(fileInputStream);
        ZipEntry entry;

        while ((entry = zipStream.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue; // We create dirs for files; no empty dirs allowed
            }

            final String entryName = entry.getName();
            final File file = new File(workDir, entryName);
            final File parent = file.getParentFile();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_065, entryName, file.getAbsolutePath());
            }

            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, parent));
            }

            final FileOutputStream outputStream = new FileOutputStream(file);

            try {
                IOUtils.copyStream(zipStream, outputStream);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
        }

        return new Bag(bagDir, true);
    }

    static File toTar(final Bag aBag) throws FileNotFoundException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_043, aBag.myDir);
        }

        final File newFile = getNewFile(aBag.myDir);
        final File parent = newFile.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, parent));
        }

        final String fileName = newFile.getName() + TAR_EXT;
        final File tarFile = new File(newFile.getParentFile(), fileName);
        final FileOutputStream tarStream = new FileOutputStream(tarFile);
        final TarOutputStream outputStream = new TarOutputStream(tarStream);
        final FilenameFilter filter = new RegexFileFilter(PATTERN);
        final File[] files = FileUtils.listFiles(aBag.myDir, filter, true);

        for (final File file : files) {
            final String entryName = getEntryName(aBag.myDir, file);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_046, entryName);
            }

            outputStream.putNextEntry(new TarEntry(file, entryName));

            final FileInputStream inputStream = new FileInputStream(file);

            try {
                IOUtils.copyStream(inputStream, outputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        IOUtils.closeQuietly(outputStream);

        return tarFile;
    }

    static Bag fromTar(final File aTarFile) throws FileNotFoundException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_042, aTarFile);
        }

        final File bagDir = getNewFile(aTarFile);
        final File workDir = bagDir.getParentFile(); // get parent b/c tar has dir

        final FileInputStream fileStream = new FileInputStream(aTarFile);
        final TarInputStream tarStream = new TarInputStream(fileStream);
        TarEntry entry;

        while ((entry = tarStream.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue; // We create dirs for files; no empty dirs allowed
            }

            final String entryName = entry.getName();
            final File file = new File(workDir, entryName);
            final File parent = file.getParentFile();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_066, entryName, file.getAbsolutePath());
            }

            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, parent));
            }

            final FileOutputStream outputStream = new FileOutputStream(file);

            try {
                IOUtils.copyStream(tarStream, outputStream);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
        }

        return new Bag(bagDir, true);
    }

    static File toTarGz(final Bag aBag) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_068, aBag.myDir);
        }

        final File newFile = getNewFile(aBag.myDir);
        final File parent = newFile.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, parent));
        }

        final String fileName = newFile.getName() + ".tar.gz";
        final File tarGzipFile = new File(newFile.getParentFile(), fileName);
        final FileOutputStream tarStream = new FileOutputStream(tarGzipFile);
        GZIPOutputStream gzipStream = null;
        FileInputStream inStream = null;

        try {
            gzipStream = new GZIPOutputStream(tarStream);
            inStream = new FileInputStream(toTar(aBag));

            IOUtils.copyStream(inStream, gzipStream);
        } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(gzipStream);
        }

        return tarGzipFile;
    }

    /**
     * Takes a bag in a tar gzip file and unpacks it into a Bag object.
     *
     * @param aTarGzipFile
     * @return A <code>Bag</code> object
     * @throws IOException
     */
    static Bag fromTarGz(final File aTarGzipFile) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_067, aTarGzipFile);
        }

        final File newFile = getNewFile(aTarGzipFile);
        final File parent = newFile.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, parent));
        }

        final String fileName = newFile.getName() + TAR_EXT;
        final File tarFile = new File(newFile.getParentFile(), fileName);
        final FileInputStream inStream = new FileInputStream(aTarGzipFile);
        final GZIPInputStream gzipStream = new GZIPInputStream(inStream);
        final FileOutputStream tarStream = new FileOutputStream(tarFile);
        IOUtils.copyStream(gzipStream, tarStream);
        final Bag bag = fromTar(tarFile);

        if (!tarFile.delete() && LOGGER.isWarnEnabled()) {
            LOGGER.warn(MessageCodes.BAGIT_045, tarFile);
        }

        return bag;
    }

    /**
     * Gets the file name relative to the path that is being put into the tar. file.
     *
     * @param aRoot A directory serving as the tar file's root directory
     * @param aFile A file to be put into the tar file
     * @return The path of the file to be included, relative to the tar's root
     */
    private static String getEntryName(final File aRoot, final File aFile) {
        final String context = aRoot.getParentFile().getAbsolutePath();
        return aFile.getAbsolutePath().substring(context.length() + 1);
    }

    /**
     * Returns a file stub for the supplied file; the stub may need to be adjusted to add a '.tar', '.tar.gz', '.zip',
     * '.tar.bz2', etc.
     *
     * @param aFile A file from which to create our new file
     * @return A new file stub we can use to create a new file
     */
    private static File getNewFile(final File aFile) {
        final String workDir = System.getProperty(Bag.WORK_DIR);
        String fileName = aFile.getName();
        final int end = fileName.indexOf('.');
        final File newFile;

        if (end != -1) {
            fileName = fileName.substring(0, end);
        }

        if (workDir != null) {
            newFile = new File(workDir, fileName);
        } else {
            newFile = new File(aFile.getParentFile(), fileName);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_048, newFile);
        }

        return newFile;
    }
}
