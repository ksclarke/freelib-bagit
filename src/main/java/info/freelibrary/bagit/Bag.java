
package info.freelibrary.bagit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

/**
 * Package structure that encapsulates descriptive tags and a payload.
 */
public class Bag {

    static final String WORK_DIR = "bagit_workdir";

    private static final Logger LOGGER = LoggerFactory.getLogger(Bag.class, Constants.BUNDLE_NAME);

    File myDir;

    private boolean isValid;

    private BagInfo myBagInfo;

    private BagData myBagData;

    private boolean myBagIsOverwritten;

    private Declaration myDeclaration;

    private PayloadManifest myManifest;

    private TagManifest myTagManifest;

    /**
     * Creates a new package from scratch or from an existing bag.
     *
     * @param aBag A bag (either a bag directory or tar, tar.bz, zip, or tar.gz file)
     * @throws IOException An exception indicating there was problem reading or writing the bag
     */
    public Bag(final File aBag) throws IOException {
        this(aBag, false);
    }

    /**
     * Creates a new package from scratch or from an existing bag. The overwrite option indicates whether an existing
     * bad directory should be changed in place or not. An overwrite value of &quot;true&quot; only makes sense for
     * bag directories; it is ignored for tar, zip, tar.gz, and tar.bz2 bags.
     *
     * @param aBag A <code>Bag</code> in file or directory form
     * @param aOverwrite A boolean indicating whether an existing directory should be overwritten
     * @throws IOException An exception indicating there was problem reading or writing the bag
     */
    public Bag(final File aBag, final boolean aOverwrite) throws IOException {
        myBagIsOverwritten = aOverwrite;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_026, aBag.getName());
        }

        // If we're using an existing bag, copy its contents into our dir
        if (aBag.exists()) {
            if (aBag.isDirectory()) {
                myDir = !aOverwrite ? createWorkingDir(aBag) : aBag;

                // We want to work on a copy rather than the original directory
                if (!aOverwrite && !myDir.mkdirs()) {
                    throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, myDir));
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.BAGIT_027);
                }

                if (!aOverwrite) {
                    FileUtils.copy(aBag, myDir);
                }
            } else {
                final String mimeDetector = MagicMimeMimeDetector.class.getName();
                MimeUtil.registerMimeDetector(mimeDetector);
                final Collection<?> types = MimeUtil.getMimeTypes(aBag);
                final MimeType type = MimeUtil.getMostSpecificMimeType(types);
                final String mimeType = type.toString();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.BAGIT_037, aBag.getName(), mimeType);
                }

                if ("application/x-gzip".equals(mimeType)) {
                    myDir = BagPackager.fromTarGz(aBag).myDir;
                    myBagIsOverwritten = true;
                } else if ("application/x-tar".equals(mimeType)) {
                    myDir = BagPackager.fromTar(aBag).myDir;
                    myBagIsOverwritten = true;
                } else if ("application/zip".equals(mimeType)) {
                    myDir = BagPackager.fromZip(aBag).myDir;
                    myBagIsOverwritten = true;
                } else if ("application/x-bzip2".equals(mimeType)) {
                    myDir = BagPackager.fromTarBZip2(aBag).myDir;
                    myBagIsOverwritten = true;
                } else {
                    final String bagPath = aBag.getAbsolutePath();

                    try {
                        clean();
                    } catch (final SecurityException details) {
                        LOGGER.warn(details.getMessage());
                    }

                    throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_017, bagPath, mimeType));
                }
            }

            setBagInfo(new BagInfo(myDir));

            try {
                myDeclaration = new Declaration(myDir);
            } catch (final FileNotFoundException details) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(details.getMessage());
                }
            }
        } else {
            myDir = !aOverwrite ? createWorkingDir(aBag) : aBag;

            // We want to work on a copy rather than the original directory
            if (!aOverwrite && !myDir.mkdirs()) {
                throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, myDir));
            }

            myDeclaration = new Declaration(myDir, false);
            myDeclaration.writeToFile();
        }

        myManifest = new PayloadManifest(myDir);
        myTagManifest = new TagManifest(myDir);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_036);
        }

        final File dataDir = new File(myDir, BagData.FILE_NAME);

        if (!dataDir.exists() && !dataDir.mkdir()) {
            try {
                if (!aOverwrite) {
                    clean();
                }
            } catch (final SecurityException details) {
                LOGGER.warn(details.getMessage());
            }

            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, dataDir));
        }

        // Add a cleanup thread to catch whatever isn't caught by finalize
        // This has small footprint, doesn't use many resources until it's run
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.BAGIT_051, myDir);
                }

                clean();
            }
        });
    }

    /**
     * Creates a new package from scratch or from an existing bag.
     *
     * @param aBagName The name of a bag (either a bag directory, new or existing, or a tar, tar.bz, zip, or tar.gz
     *        file)
     * @throws IOException An exception indicating there was problem reading or writing the bag
     */
    public Bag(final String aBagName) throws IOException {
        this(new File(aBagName));
    }

    /**
     * Creates a new package from scratch or from an existing bag. The overwrite option indicates whether an existing
     * bad directory should be changed in place or not. An overwrite value of &quot;true&quot; only makes sense for
     * bag directories; it is ignored for tar, zip, tar.gz, and tar.bz2 bags.
     *
     * @param aBagName The name of a bag (either a bag directory, new or existing, or a tar, tar.bz, zip, or tar.gz
     *        file)
     * @param aOverwrite Whether a bag's contents should be overwritten
     * @throws IOException An exception indicating there was problem reading or writing the bag
     */
    public Bag(final String aBagName, final boolean aOverwrite) throws IOException {
        this(new File(aBagName), aOverwrite);
    }

    /**
     * Creates a new package from scratch or from an existing bag and populates the bag-info.txt with values from the
     * supplied <code>Properties</code>.
     *
     * @param aBagName The name of a bag (either a bag directory, new or existing, or a tar, tar.bz, zip, or tar.gz
     *        file)
     * @param aProperties Metadata values to be added to the bag-info.txt
     * @throws IOException An exception indicating there was problem reading or writing the bag
     */
    public Bag(final String aBagName, final Properties aProperties) throws IOException {
        this(new File(aBagName));
        setBagInfo(new BagInfo(aProperties));
    }

    /**
     * Creates a new package from scratch or from an existing bag and populates the bag-info.txt with values from the
     * supplied <code>Properties</code>.
     *
     * @param aBag A bag (either a bag directory, new or existing, or a tar, tar.bz, zip, or tar.gz file)
     * @param aProperties Metadata values to be added to the bag-info.txt
     * @throws IOException An exception indicating there was problem reading or writing the bag
     */
    public Bag(final File aBag, final Properties aProperties) throws IOException {
        this(aBag);
        setBagInfo(new BagInfo(aProperties));
    }

    /**
     * Gets a representation of the bag's payload. However, files can be added to the bag by using
     * <code>addData(File)</code> without having to get the <code>BagData</code> object.
     *
     * @return Bag data
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
     * @throws RuntimeException If the bag to which we're adding files has already been validated
     */
    public void addData(final File... aFiles) throws IOException, I18nRuntimeException {
        if (isValid) {
            throw new I18nRuntimeException(Constants.BUNDLE_NAME, MessageCodes.BAGIT_014);
        }

        final File dataDir = new File(myDir, BagData.FILE_NAME);

        for (final File fromFile : aFiles) {
            final File toFile = new File(dataDir, fromFile.getName());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_029, fromFile.getName());
            }

            FileUtils.copy(fromFile, toFile);
            myManifest.updateWith(toFile);
        }
    }

    /**
     * Completes the bag structure.
     *
     * @return Bag
     * @throws IOException If there is difficulty writing the missing files
     */
    public Bag complete() throws IOException {
        final File dataDir = new File(myDir, BagData.FILE_NAME);

        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_005, dataDir));
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
     * @throws IOException If there is a problem writing the bag to the file system
     */
    public File toDir() throws IOException {
        if (myBagIsOverwritten) {
            return myDir;
        } else {
            final File dir = myDir.getParentFile();
            final String name = myDir.getName();
            final int end = name.lastIndexOf('_');
            final File bagDir = new File(dir.getParentFile(), name.substring(0, end));

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
     * @throws RuntimeException If the bag to which the <code>BagInfo</code> is being added has already been validated
     */
    public void setBagInfo(final BagInfo aBagInfo) throws I18nRuntimeException {
        if (isValid) {
            throw new I18nRuntimeException(Constants.BUNDLE_NAME, MessageCodes.BAGIT_014);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_030);
        }

        myBagInfo = aBagInfo;
    }

    /**
     * Gets the &quot;octetstream sum&quot; of the payload, namely, a two- part number of the form
     * "OctetCount.StreamCount", where OctetCount is the total number of octets (8-bit bytes) across all payload file
     * content and StreamCount is the total number of payload files.
     *
     * @return The &quot;octetstream sum&quot; of the payload
     */
    public String getPayloadOxum() {
        final BagData bagData = getBagData();
        long bytes = 0;
        int count = 0;

        for (final String fileName : bagData.getFilePaths()) {
            try {
                final long size = bagData.getSize(fileName);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.BAGIT_025, size, fileName);
                }

                bytes += size;
                count += 1;
            } catch (final FileNotFoundException details) {
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
    @Override
    public String toString() {
        final String eol = System.getProperty("line.separator");
        final StringBuilder builder = new StringBuilder(25);

        builder.append("<bag name=\"").append(myDir.getName()).append("\">").append(eol);

        try {
            builder.append(getDeclaration().toString());
        } catch (final IOException details) {
            throw new RuntimeException(details);
        }

        builder.append(getBagInfo().toString()).append(getManifest().toString()).append(getTagManifest().toString())
                .append(getBagData().toString()).append("</bag>");

        return builder.toString();
    }

    /**
     * Cleans up all the temporary files created during the manipulation of the bag.
     */
    private void clean() {
        final String clean = System.getProperty(Constants.AUTOCLEAN, Boolean.TRUE.toString());

        if (Boolean.TRUE.toString().equals(clean) && myDir != null && myDir.exists()) {
            final File workDir = myBagIsOverwritten ? myDir : myDir.getParentFile();

            if (!workDir.exists()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.BAGIT_049, workDir);
                }

                return;
            }

            LOGGER.debug(MessageCodes.BAGIT_015, workDir);

            if (workDir.exists() && !FileUtils.delete(workDir)) {
                LOGGER.warn(MessageCodes.BAGIT_069);
            }
        }
    }

    private File createWorkingDir(final File aBagDir) {
        final String workDirPath = System.getProperty(Bag.WORK_DIR);
        final String fileName = aBagDir.getName() + "_" + new Date().getTime();
        final File workDir;

        // TODO: check system space to confirm we'll have space to continue

        // If we have a work directory, use that
        if (workDirPath != null) {
            workDir = new File(workDirPath);
        } else {
            workDir = aBagDir.getParentFile();
        }

        // Create a temporary working directory for our bag
        final File tmpBagDir = new File(workDir, fileName);
        return new File(tmpBagDir, aBagDir.getName());
    }

    Declaration getDeclaration() throws IOException {
        if (myDeclaration == null) {
            myDeclaration = new Declaration(myDir, false);
        }

        return myDeclaration;
    }

    @Override
    protected void finalize() throws Throwable {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.BAGIT_050, myDir);
        }

        clean();

        super.finalize();
    }

    PayloadManifest getManifest() {
        if (myManifest == null) {
            try {
                myManifest = new PayloadManifest(myDir);
            } catch (final IOException details) {
                throw new RuntimeException(details); // shouldn't happen
            }
        }

        return myManifest;
    }

    TagManifest getTagManifest() {
        if (myTagManifest == null) {
            try {
                myTagManifest = new TagManifest(myDir);
            } catch (final IOException details) {
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
