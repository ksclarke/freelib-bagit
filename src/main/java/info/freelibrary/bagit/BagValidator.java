
package info.freelibrary.bagit;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.RegexFileFilter;

/**
 * Bag validator that can determine whether bags are complete and/or valid.
 */
public class BagValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BagValidator.class, Constants.BUNDLE_NAME);

    /**
     * Checks whether a supplied bag is complete or not.
     *
     * @param aBag A bag to check for completeness
     * @return True if the bag is complete; else, false
     * @throws IOException If there is trouble reading or writing the bag
     */
    public boolean isComplete(final Bag aBag) throws IOException {
        try {
            checkStructure(aBag);
        } catch (final BagException details) {
            LOGGER.warn(MessageCodes.BAGIT_016, aBag.myDir.getName(), details.getMessage());
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
    public boolean isValid(final Bag aBag) throws IOException {
        try {
            validate(aBag);
            return true;
        } catch (final BagException details) {
            return false;
        }
    }

    /**
     * Validates a bag, throwing a <code>BagException</code> if the supplied bag isn't valid.
     *
     * @param aBag A bag to check for validity
     * @return A validated bag which can be written in a variety of formats
     * @throws BagException If the supplied bag doesn't validate
     * @throws IOException If there is a problem reading or writing the files in the bag
     */
    public ValidBag validate(final Bag aBag) throws BagException, IOException {
        checkStructure(aBag);

        // Validity is determined by whether the checksums are actually correct
        final PayloadManifest payloadManifest = aBag.getManifest();
        final String algorithm = payloadManifest.getHashAlgorithm();

        for (final File payloadFile : payloadManifest.getFiles()) {
            try {
                final String hash = payloadManifest.getStoredHash(payloadFile);
                final String check = FileUtils.hash(payloadFile, algorithm);

                if (!hash.equals(check)) {
                    throw new BagException(MessageCodes.BAGIT_021, payloadFile.getAbsolutePath(), hash, check);
                }
            } catch (final NoSuchAlgorithmException details) {
                throw new I18nRuntimeException(details);
            }
        }

        final TagManifest tagManifest = aBag.getTagManifest();
        final String tagAlgorithm = tagManifest.getHashAlgorithm();

        for (final File tagFile : tagManifest.getFiles()) {
            try {
                final String hash = tagManifest.getStoredHash(tagFile);
                final String check = FileUtils.hash(tagFile, tagAlgorithm);

                if (!hash.equals(check)) {
                    throw new BagException(MessageCodes.BAGIT_020, tagFile.getAbsolutePath(), hash, check);
                }
            } catch (final NoSuchAlgorithmException details) {
                throw new I18nRuntimeException(details);
            }
        }

        // If we have a BagInfo, let's supply Payload-Oxum and Bag-Size
        final BagInfo bagInfo = aBag.getBagInfo();

        if (bagInfo.countTags() > 0) {
            bagInfo.removeMetadata(BagInfoTags.BAG_SIZE);
            bagInfo.removeMetadata(BagInfoTags.PAYLOAD_OXUM);

            bagInfo.addMetadata(BagInfoTags.BAG_SIZE, FileUtils.sizeFromBytes(aBag.getSize(), true));
            bagInfo.addMetadata(BagInfoTags.PAYLOAD_OXUM, aBag.getPayloadOxum());

            // If we change, we need to recalculate the file's checksum
            try {
                final File bagInfoFile = new File(aBag.myDir, BagInfo.FILE_NAME);

                bagInfo.writeTo(bagInfoFile);
                tagManifest.remove(bagInfoFile);
                tagManifest.add(bagInfoFile);
            } catch (final NoSuchAlgorithmException details) {
                throw new I18nRuntimeException(details);
            }
        }

        return new ValidBag(aBag);
    }

    /**
     * Checks the payload and payload manifest to make sure they are in sync.
     *
     * @param aManifest A manifest representing the payload files
     * @param aDataDir A bag data directory
     * @throws BagException If there is a sync problem between manifest and payload
     * @throws IOException If there is trouble reading or writing the payload
     */
    private void checkPayload(final PayloadManifest aManifest, final File aDataDir) throws BagException, IOException {
        final FilenameFilter filter = new RegexFileFilter(".*");
        final File[] files = FileUtils.listFiles(aDataDir, filter, true);
        final File[] payloadFiles = aManifest.getFiles();

        if (files.length != payloadFiles.length) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_024, Integer.toString(files.length), Integer.toString(
                        payloadFiles.length));
            }

            throw new BagException(MessageCodes.BAGIT_018);
        }

        Arrays.sort(files);
        Arrays.sort(payloadFiles);

        for (int index = 0; index < files.length; index++) {
            final String fPath = files[index].getAbsolutePath();
            final String mPath = payloadFiles[index].getAbsolutePath();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_063, fPath, mPath);
            }

            if (!fPath.equals(mPath)) {
                throw new BagException(MessageCodes.BAGIT_018);
            }
        }
    }

    private void checkRequiredFiles(final Bag aBag) throws BagException {
        if (!aBag.hasDeclaration()) {
            throw new BagException(MessageCodes.BAGIT_070);
        } else {
            try {
                final Declaration declaration = aBag.getDeclaration();
                declaration.validate();
                declaration.writeToFile();
            } catch (final IOException details) {
                throw new BagException(MessageCodes.BAGIT_003, details);
            }
        }

        // We check actual files in the checkPayload method, run after this one
        final int entryCount = aBag.getManifest().countEntries();
        final int dataFileCount = aBag.getBagData().fileCount();

        if (entryCount != dataFileCount) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.BAGIT_024, Integer.toString(dataFileCount), Integer.toString(entryCount));
            }

            throw new BagException(MessageCodes.BAGIT_022);
        }
    }

    /**
     * Checks the structure of the bag directory to make sure it is complete.
     *
     * @param aBag A bag to check for completeness
     * @throws BagException If there is a structural problem with the bag
     * @throws IOException If there is trouble reading or writing files in the bag
     */
    private void checkStructure(final Bag aBag) throws BagException, IOException {
        checkRequiredFiles(aBag);
        checkPayload(aBag.getManifest(), new File(aBag.myDir, BagData.FILE_NAME));
        checkTagManifest(aBag.getTagManifest());
    }

    private void checkTagManifest(final TagManifest aTagManifest) throws BagException, IOException {
        for (final File tagFile : aTagManifest.getFiles()) {
            if (!tagFile.exists()) {
                throw new BagException(MessageCodes.BAGIT_019, tagFile);
            }
        }
    }

}
