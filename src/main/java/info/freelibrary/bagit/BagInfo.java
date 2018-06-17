
package info.freelibrary.bagit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import info.freelibrary.util.BufferedFileWriter;
import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

/**
 * Metadata elements describing the bag and payload. The metadata elements, all optional, are intended primarily for
 * human readability.
 */
public class BagInfo {

    static final String FILE_NAME = "bag-info.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(BagInfo.class, Constants.BUNDLE_NAME);

    private static final String METADATA_DELIM = ": ";

    /**
     * Whether the bag-info has been validated or not yet.
     */
    boolean isValid;

    private final List<Metadata> myMetadata;

    /**
     * Creates a new <code>BagInfo</code> for <code>Bag</code> metadata.
     */
    public BagInfo() {
        myMetadata = new ArrayList<>();
    }

    /**
     * Creates a new <code>BagInfo</code> from a Java <code>Properties</code> object.
     *
     * @param aProperties A Java <code>Properties</code> from which to build the <code>BagInfo</code>
     */
    public BagInfo(final Properties aProperties) {
        final Iterator<String> props = aProperties.stringPropertyNames().iterator();

        myMetadata = new ArrayList<>();

        while (props.hasNext()) {
            final String name = props.next();
            final String value = aProperties.getProperty(name);

            if (StringUtils.trimToNull(value) != null) {
                myMetadata.add(new Metadata(name, value));
            }
        }
    }

    /**
     * Creates a BagInfo for Bag metadata from the metadata of another BagInfo.
     *
     * @param aBagInfo A bag info
     */
    public BagInfo(final BagInfo aBagInfo) {
        final Iterator<Metadata> iterator = aBagInfo.myMetadata.iterator();

        myMetadata = new ArrayList<>();

        while (iterator.hasNext()) {
            myMetadata.add(iterator.next().copy());
        }
    }

    /**
     * Creates a new <code>BagInfo</code> for <code>Bag</code> metadata from an existing <code>bag-info.txt</code>
     * file. If a bag directory is passed in, the <code>bag-info.txt</code> is located and used.
     *
     * @param aFile An existing <code>bag-info.txt</code> file or the <code>BagIt</code> directory
     * @throws IOException If there is trouble reading the <code>bag-info.txt</code> file
     */
    BagInfo(final File aFile) throws IOException {
        final File bagInfo = new File(aFile, FILE_NAME);
        myMetadata = new ArrayList<>();

        if (bagInfo.exists()) {
            readFrom(bagInfo);
        } else {
            if (!bagInfo.createNewFile()) {
                throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_007, bagInfo));
            }
        }
    }

    /**
     * Creates a new <code>BagInfo</code> from this one.
     *
     * @return Bag info
     */
    public BagInfo copy() {
        return new BagInfo(this);
    }

    /**
     * Tests whether the <code>BagInfo</code> contains the supplied tag.
     *
     * @param aTag A tag to test for occurrence
     * @return True if the supplied tag can be found; else, false
     */
    public final boolean containsTag(final String aTag) {
        for (int index = 0; index < myMetadata.size(); index++) {
            if (aTag.equals(myMetadata.get(index).myTag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the number of tags found in this <code>BagInfo</code>.
     *
     * @return The number of tags found in this <code>BagInfo</code>
     */
    public final int countTags() {
        return myMetadata.size();
    }

    /**
     * Returns an array of tags found in this <code>BagInfo</code>.
     *
     * @return An array of tags
     */
    public final String[] getTags() {
        final String[] tags = new String[myMetadata.size()];

        for (int index = 0; index < tags.length; index++) {
            tags[index] = myMetadata.get(index).myTag;
        }

        return tags;
    }

    /**
     * Returns the value for the supplied tag or null if the tag isn't found.
     *
     * @param aTag The tag whose value we want to learn
     * @return The value for the supplied tag
     */
    public final String getValue(final String aTag) {
        for (int index = 0; index < myMetadata.size(); index++) {
            final Metadata metadata = myMetadata.get(index);

            if (aTag.equals(metadata.myTag)) {
                return metadata.myValue;
            }
        }

        return null;
    }

    /**
     * Returns the number of values found for a particular tag.
     *
     * @param aTag The tag to locate
     * @return The number of values found for the supplied tag
     */
    public final int countTags(final String aTag) {
        return getValues(aTag).length;
    }

    /**
     * Returns all the values for the supplied tag or an empty array if the tag isn't found.
     *
     * @param aTag The tag whose value we want to learn
     * @return The value for the supplied tag
     */
    public final String[] getValues(final String aTag) {
        final ArrayList<String> matches = new ArrayList<>();

        for (int index = 0; index < myMetadata.size(); index++) {
            final Metadata metadata = myMetadata.get(index);

            if (aTag.equals(metadata.myTag)) {
                matches.add(metadata.myValue);
            }
        }

        return matches.toArray(new String[matches.size()]);
    }

    /**
     * Returns the value for the supplied tag; if the tag isn't found the supplied default value is returned.
     *
     * @param aTag The tag whose value we want to learn
     * @param aDefaultValue The default value to return if the supplied tag isn't found
     * @return The value for the supplied tag or the default value
     */
    public final String getValue(final String aTag, final String aDefaultValue) {
        final String value = getValue(aTag);
        return value == null ? aDefaultValue : value;
    }

    /**
     * Returns the value of the metadata element at the supplied index position.
     *
     * @param aIndex The index position from which we want the value
     * @return The value of the metadata element at the supplied index position
     */
    public final String getValue(final int aIndex) {
        return myMetadata.get(aIndex).myValue;
    }

    /**
     * Returns the tag of the metadata element at the supplied index position.
     *
     * @param aIndex The index position from which we want the tag
     * @return The tag of the metadata element at the supplied index position
     */
    public final String getTag(final int aIndex) {
        return myMetadata.get(aIndex).myTag;
    }

    /**
     * Removes all metadata elements that match the supplied tag.
     *
     * @param aTag The tag whose metadata element we want to remove
     * @return True if the metadata elements were successfully removed; else, false
     */
    public final boolean removeMetadata(final String aTag) {
        if (isValid) {
            throw new I18nRuntimeException(Constants.BUNDLE_NAME, MessageCodes.BAGIT_014);
        }

        int totalCount = myMetadata.size();

        for (int index = 0; index < totalCount; index++) {
            final Metadata metadata = myMetadata.get(index);

            if (aTag.equals(metadata.myTag)) {
                if (myMetadata.remove(index) == null) {
                    return false;
                }

                totalCount -= 1;
            }
        }

        return totalCount < myMetadata.size();
    }

    /**
     * Sets the <code>BagInfo</code> metadata (tag and value) supplied.
     *
     * @param aTag A metadata tag
     * @param aValue A metadata value
     * @return True if metadata was added; false, if it couldn't be added
     */
    public final boolean addMetadata(final String aTag, final String aValue) {
        if (isValid) {
            throw new I18nRuntimeException(Constants.BUNDLE_NAME, MessageCodes.BAGIT_014);
        }

        return myMetadata.add(new Metadata(aTag, aValue));
    }

    /**
     * Outputs an XML representation of the <code>BagInfo</code>, useful for debugging.
     *
     * @return An XML representation of the <code>BagInfo</code>
     */
    @Override
    public String toString() {
        final String eol = System.getProperty("line.separator");
        final Iterator<Metadata> iterator = myMetadata.iterator();
        final StringBuilder builder = new StringBuilder(60);

        builder.append("<bagInfo>").append(eol);

        while (iterator.hasNext()) {
            final Metadata metadata = iterator.next();

            builder.append("<metadata tag=\"").append(metadata.myTag).append("\" value=\"").append(metadata.myValue)
                    .append("\" />").append(eol);
        }

        builder.append("</bagInfo>").append(eol);

        return builder.toString();
    }

    private void readFrom(final File aBagInfoFile) throws IOException {
        final FileReader fileReader = new FileReader(aBagInfoFile);
        final BufferedReader reader = new BufferedReader(fileReader);
        final String whitespacePattern = "^\\s.*";
        String lastProp = null;
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (line.matches(whitespacePattern)) {
                    if (lastProp != null) {
                        line = line.replaceFirst(whitespacePattern, " ");
                        addMetadata(lastProp, getValue(lastProp) + line);
                    }
                } else {
                    final String[] parts = line.split(METADATA_DELIM);

                    if (parts.length == 2) {
                        lastProp = parts[0].trim();
                        addMetadata(lastProp, parts[1]);
                    } else {
                        // don't throw exception here; postpone to validation
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn(MessageCodes.BAGIT_008, aBagInfoFile.getAbsolutePath(), line);
                        }
                    }
                }
            }
        } finally {
            reader.close();
        }
    }

    void writeTo(final File aBagInfoFile) throws IOException {
        final BufferedFileWriter writer = new BufferedFileWriter(aBagInfoFile);

        try {
            for (int index = 0; index < myMetadata.size(); index++) {
                final Metadata metadata = myMetadata.get(index);

                writer.append(metadata.myTag).append(METADATA_DELIM);
                writer.append(metadata.myValue);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.BAGIT_028, metadata.myTag, metadata.myValue);
                }

                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private static final class Metadata implements Cloneable {

        private final String myTag;

        private final String myValue;

        private Metadata(final String aTag, final String aValue) {
            Objects.requireNonNull(aTag);
            Objects.requireNonNull(aValue);

            myTag = aTag;
            myValue = aValue;
        }

        public Metadata copy() {
            return new Metadata(myTag, myValue);
        }

        @Override
        public String toString() {
            return myTag + METADATA_DELIM + myValue;
        }
    }
}
