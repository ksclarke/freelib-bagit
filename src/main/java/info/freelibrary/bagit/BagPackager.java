package info.freelibrary.bagit;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.bzip2.CBZip2InputStream;
import info.freelibrary.util.bzip2.CBZip2OutputStream;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;
import org.xeustechnologies.jtar.TarOutputStream;

/**
 * A bag packager that prepares bags for transmission.
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
class BagPackager extends I18nObject {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BagPackager.class);

	private static final BagPackager PKGR = new BagPackager();

	private BagPackager() {}

	static File toTarBZip2(Bag aBag) throws FileNotFoundException, IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.to_tar_bz2", aBag.myDir));
		}

		File tarFile = toTar(aBag);
		File newFile = getNewFile(aBag.myDir);
		String fileName = newFile.getName() + ".tar.bz2";
		File tarBz2File = new File(newFile.getParentFile(), fileName);
		CBZip2OutputStream bz2Stream = null;
		FileOutputStream outStream = null;
		FileInputStream inStream = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.to_bz2", tarFile));
		}

		try {
			outStream = new FileOutputStream(tarBz2File);
			inStream = new FileInputStream(tarFile);
			bz2Stream = new CBZip2OutputStream(outStream);

			IOUtils.copyStream(inStream, bz2Stream);
		}
		finally {
			IOUtils.closeQuietly(inStream);
			IOUtils.closeQuietly(bz2Stream);
		}

		if (!tarFile.delete()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.tar_delete", tarFile));
		}

		return tarBz2File;
	}

	static Bag fromTarBZip2(File aBZip2File) throws FileNotFoundException,
			IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.from_tar_bz2", aBZip2File));
		}

		File newFile = getNewFile(aBZip2File);
		String fileName = newFile.getName() + ".tar";
		File tarFile = new File(newFile.getParentFile(), fileName);
		FileInputStream fileInputStream = new FileInputStream(aBZip2File);
		CBZip2InputStream bz2Stream = new CBZip2InputStream(fileInputStream);
		FileOutputStream fileOutputStream = new FileOutputStream(tarFile);
		IOUtils.copyStream(bz2Stream, fileOutputStream);
		Bag bag = fromTar(tarFile);

		if (!tarFile.delete() && LOGGER.isWarnEnabled()) {
			LOGGER.warn(PKGR.getI18n("bagit.debug.tar_delete", tarFile));
		}

		return bag;
	}

	static File toZip(Bag aBag) throws FileNotFoundException, IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.to_zip", aBag.myDir));
		}

		File newFile = getNewFile(aBag.myDir);
		String fileName = newFile.getName() + ".zip";
		File zipFile = new File(newFile.getParentFile(), fileName);
		FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
		ZipOutputStream zipStream = new ZipOutputStream(fileOutputStream);
		FilenameFilter filter = new RegexFileFilter(".*");
		File[] files = FileUtils.listFiles(aBag.myDir, filter, true);

		for (File file : files) {
			String entryName = getEntryName(aBag.myDir, file);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(PKGR.getI18n("bagit.debug.writing_zip_entry",
						entryName));
			}

			zipStream.putNextEntry(new ZipEntry(entryName));
			
			FileInputStream inputStream = new FileInputStream(file);
			
			try {
				IOUtils.copyStream(inputStream, zipStream);
			}
			finally {
				IOUtils.closeQuietly(inputStream);
			}
		}

		IOUtils.closeQuietly(zipStream);

		return zipFile;
	}

	static Bag fromZip(File aZipFile) throws FileNotFoundException, IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.from_zip", aZipFile));
		}

		File bagDir = getNewFile(aZipFile);
		File workDir = bagDir.getParentFile(); // get parent b/c zip has dir
		FileInputStream fileInputStream = new FileInputStream(aZipFile);
		ZipInputStream zipStream = new ZipInputStream(fileInputStream);
		ZipEntry entry;

		while ((entry = zipStream.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				continue; // We create dirs for files; no empty dirs allowed
			}

			String entryName = entry.getName();
			File file = new File(workDir, entryName);
			File parent = file.getParentFile();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(PKGR.getI18n("bagit.debug.reading_zip_entry",
						new String[] { entryName, file.getAbsolutePath() }));
			}

			// Create the dirs needed for file
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IOException(PKGR.getI18n("bagit.dir_create", parent));
			}

			FileOutputStream outputStream = new FileOutputStream(file);
			
			try {
				IOUtils.copyStream(zipStream, outputStream);
			}
			finally {
				IOUtils.closeQuietly(outputStream);
			}
		}

		return new Bag(bagDir, true);
	}

	static File toTar(Bag aBag) throws FileNotFoundException, IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.to_tar", aBag.myDir));
		}

		File newFile = getNewFile(aBag.myDir);
		String fileName = newFile.getName() + ".tar";
		File tarFile = new File(newFile.getParentFile(), fileName);
		FileOutputStream tarStream = new FileOutputStream(tarFile);
		TarOutputStream outputStream = new TarOutputStream(tarStream);
		FilenameFilter filter = new RegexFileFilter(".*");
		File[] files = FileUtils.listFiles(aBag.myDir, filter, true);

		for (File file : files) {
			String entryName = getEntryName(aBag.myDir, file);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(PKGR.getI18n("bagit.debug.writing_tar_entry",
						entryName));
			}

			outputStream.putNextEntry(new TarEntry(file, entryName));
			
			FileInputStream inputStream = new FileInputStream(file);
			
			try {
				IOUtils.copyStream(inputStream, outputStream);
			}
			finally {
				IOUtils.closeQuietly(inputStream);
			}
		}

		IOUtils.closeQuietly(outputStream);

		return tarFile;
	}

	static Bag fromTar(File aTarFile) throws FileNotFoundException, IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.from_tar", aTarFile));
		}

		File bagDir = getNewFile(aTarFile);
		File workDir = bagDir.getParentFile(); // get parent b/c tar has dir

		FileInputStream fileStream = new FileInputStream(aTarFile);
		TarInputStream tarStream = new TarInputStream(fileStream);
		TarEntry entry;

		while ((entry = tarStream.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				continue; // We create dirs for files; no empty dirs allowed
			}

			String entryName = entry.getName();
			File file = new File(workDir, entryName);
			File parent = file.getParentFile();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(PKGR.getI18n("bagit.debug.reading_tar_entry",
						new String[] { entryName, file.getAbsolutePath() }));
			}

			// Create the dirs needed for file
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IOException(PKGR.getI18n("bagit.dir_create", parent));
			}

			FileOutputStream outputStream = new FileOutputStream(file);

			try {
				IOUtils.copyStream(tarStream, outputStream);
			}
			finally {
				IOUtils.closeQuietly(outputStream);
			}
		}

		return new Bag(bagDir, true);
	}

	static File toTarGz(Bag aBag) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.to_tar_gz", aBag.myDir));
		}

		File newFile = getNewFile(aBag.myDir);
		String fileName = newFile.getName() + ".tar.gz";
		File tarGzipFile = new File(newFile.getParentFile(), fileName);
		FileOutputStream tarStream = new FileOutputStream(tarGzipFile);
		GZIPOutputStream gzipStream = null;
		FileInputStream inStream = null;

		try {
			gzipStream = new GZIPOutputStream(tarStream);
			inStream = new FileInputStream(toTar(aBag));

			IOUtils.copyStream(inStream, gzipStream);
		}
		finally {
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
	static Bag fromTarGz(File aTarGzipFile) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.from_tar_gz", aTarGzipFile));
		}

		File newFile = getNewFile(aTarGzipFile);
		String fileName = newFile.getName() + ".tar";
		File tarFile = new File(newFile.getParentFile(), fileName);
		FileInputStream inStream = new FileInputStream(aTarGzipFile);
		GZIPInputStream gzipStream = new GZIPInputStream(inStream);
		FileOutputStream tarStream = new FileOutputStream(tarFile);
		IOUtils.copyStream(gzipStream, tarStream);
		Bag bag = fromTar(tarFile);

		if (!tarFile.delete() && LOGGER.isWarnEnabled()) {
			LOGGER.warn(PKGR.getI18n("bagit.debug.tar_delete", tarFile));
		}

		return bag;
	}

	/**
	 * Gets the file name relative to the path that is being put into the tar.
	 * file.
	 * 
	 * @param aRoot A directory serving as the tar file's root directory
	 * @param aFile A file to be put into the tar file
	 * @return The path of the file to be included, relative to the tar's root
	 */
	private static String getEntryName(File aRoot, File aFile) {
		String context = aRoot.getParentFile().getAbsolutePath();
		return aFile.getAbsolutePath().substring(context.length() + 1);
	}

	/**
	 * Returns a file stub for the supplied file; the stub may need to be
	 * adjusted to add a '.tar', '.tar.gz', '.zip', '.tar.bz2', etc.
	 * 
	 * @param aFile A file from which to create our new file
	 * @return A new file stub we can use to create a new file
	 */
	private static File getNewFile(File aFile) {
		String workDir = System.getProperty(Bag.WORK_DIR);
		String fileName = aFile.getName();
		int end = fileName.indexOf(".");
		File newFile;

		if (end != -1) {
			fileName = fileName.substring(0, end);
		}

		if (workDir != null) {
			newFile = new File(workDir, fileName);
		}
		else {
			newFile = new File(aFile.getParentFile(), fileName);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(PKGR.getI18n("bagit.debug.get_new_file", newFile));
		}

		return newFile;
	}

}
