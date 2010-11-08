package info.freelibrary.bagit;

import info.freelibrary.util.BufferedFileWriter;
import info.freelibrary.util.I18nObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tag file that contains metadata elements describing the bag and the
 * payload. The metadata elements are intended primarily for human readability.
 * All metadata elements are optional.
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
public class BagInfo extends I18nObject implements BagInfoConstants {

	private static final String FILE_NAME = "bag-info.txt";

	private static final Logger LOGGER = LoggerFactory.getLogger(BagInfo.class);

	/**
	 * A tag translator so &quot;Bag-Size&quot;, &quot;bag-size&quot;, &quot;bag
	 * size&quot;, and &quot;Bag_size&quot;, etc. are all seen as the same tag.
	 */
	private static final TreeSet<String> myTagTranslator = new TreeSet<String>(
			new Comparator<String>() {

				@Override
				public int compare(String aFirstString, String aSecondString) {
					String first = aFirstString.replaceAll("[-_\\.]", " ");
					String second = aSecondString.replaceAll("[-_\\.]", " ");
					return first.compareToIgnoreCase(second);
				}

			});

	/**
	 * Properties, or metadata tags/values, stored in this <code>BagInfo</code>.
	 */
	private Properties myProperties;

	boolean isValid;

	/**
	 * Creates a new <code>BagInfo</code> for <code>Bag</code> metadata.
	 */
	public BagInfo() {
		myProperties = new Properties();
		initTagTranslator();
	}

	/**
	 * Creates a new <code>BagInfo</code> from a Java <code>Properties</code>
	 * object.
	 * 
	 * @param aProperties A Java <code>Properties</code> from which to build the
	 *        <code>BagInfo</code>
	 */
	public BagInfo(Properties aProperties) {
		myProperties = new Properties(aProperties); // doesn't copy properties

		Iterator<String> props = aProperties.stringPropertyNames().iterator();

		while (props.hasNext()) {
			String name = props.next();
			myProperties.put(name, aProperties.getProperty(name));
		}

		initTagTranslator();
	}

	/**
	 * Creates a new <code>BagInfo</code> for <code>Bag</code> metadata from an
	 * existing <code>bag-info.txt</code> file. If a bag directory is passed in,
	 * the <code>bag-info.txt</code> is located and used.
	 * 
	 * @param aFile An existing <code>bag-info.txt</code> file or the
	 *        <code>BagIt</code> directory
	 * @throws IOException If there is trouble reading the
	 *         <code>bag-info.txt</code> file
	 */
	BagInfo(File aFile) throws IOException {
		File bagInfo = new File(aFile, FILE_NAME);

		myProperties = new Properties();

		if (bagInfo.exists()) {
			readFrom(bagInfo);
		}
		else {
			if (!bagInfo.createNewFile()) {
				throw new IOException(getI18n("bagit.baginfo_create", bagInfo));
			}
		}

		initTagTranslator();
	}

	/**
	 * Creates a new <code>BagInfo</code> from this one.
	 */
	public BagInfo clone() {
		return new BagInfo(myProperties);
	}

	/**
	 * Tests whether the <code>BagInfo</code> contains the supplied tag.
	 * 
	 * @param aTag A tag to test for occurrence
	 * @return True if the supplied tag can be found; else, false
	 */
	public boolean containsTag(String aTag) {
		return myProperties.containsKey(aTag);
	}

	/**
	 * Returns the number of tags found in this <code>BagInfo</code>.
	 * 
	 * @return The number of tags found in tihs <code>BagInfo</code>
	 */
	public int countTags() {
		return myProperties.size();
	}

	public Iterator<String> getTags() {
		return myProperties.stringPropertyNames().iterator();
	}

	/**
	 * Returns the value for the supplied tag.
	 * 
	 * @param aTag The tag whose value we want to learn
	 * @return The value for the supplied tag
	 */
	public String getValue(String aTag) {
		return myProperties.getProperty(aTag);
	}

	/**
	 * Returns the value for the supplied tag; if the tag isn't found the
	 * supplied default value is returned.
	 * 
	 * @param aTag The tag whose value we want to learn
	 * @param aDefaultValue The default value to return if the supplied tag
	 *        isn't found
	 * @return The value for the supplied tag or the default value
	 */
	public String getValue(String aTag, String aDefaultValue) {
		return myProperties.getProperty(aTag, aDefaultValue);
	}

	/**
	 * Removes the metadata element for the supplied tag.
	 * 
	 * @param aTag The tag whose metadata element we want to remove
	 * @return True if the metadata was successfully removed; else, false
	 */
	public boolean removeMetadata(String aTag) {
		if (isValid) {
			throw new RuntimeException(getI18n("bagit.validated"));
		}

		return myProperties.remove(aTag) != null;
	}

	/**
	 * Sets the <code>BagInfo</code> metadata (tag and value) supplied.
	 * 
	 * @param aTag A metadata tag
	 * @param aValue A metadata value
	 */
	public void setMetadata(String aTag, String aValue) {
		if (isValid) {
			throw new RuntimeException(getI18n("bagit.validated"));
		}

		myProperties.setProperty(aTag, aValue);
	}

	/**
	 * Outputs an XML representation of the <code>BagInfo</code>, useful for
	 * debugging.
	 * 
	 * @return An XML representation of the <code>BagInfo</code>
	 */
	public String toString() {
		String eol = System.getProperty("line.separator");
		Enumeration<?> keys = myProperties.keys();
		StringBuilder builder = new StringBuilder();

		builder.append("<bagInfo>").append(eol);

		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = myProperties.getProperty(key);

			builder.append("<metadata tag=\"").append(key);
			builder.append("\" value=\"").append(value);
			builder.append("\" />").append(eol);
		}

		builder.append("</bagInfo>").append(eol);

		return builder.toString();
	}

	/**
	 * A tag translator to treat variations in tag name consistently.
	 */
	private void initTagTranslator() {
		if (myTagTranslator.size() == 0) {
			myTagTranslator.addAll(Arrays.asList(new String[] { SOURCE_ORG_TAG,
					SENDER_IDENTIFIER_TAG, SENDER_DESCRIPTION_TAG,
					PAYLOAD_OXUM_TAG, ORG_ADDRESS_TAG, EXT_IDENTIFIER_TAG,
					EXT_DESCRIPTION_TAG, CONTACT_PHONE_TAG, CONTACT_NAME_TAG,
					CONTACT_EMAIL_TAG, BAGGING_DATE_TAG, BAG_SIZE_TAG,
					BAG_GROUP_ID_TAG, BAG_COUNT_TAG }));
		}
	}

	private void readFrom(File aBagInfoFile) throws IOException {
		FileReader fileReader = new FileReader(aBagInfoFile);
		BufferedReader reader = new BufferedReader(fileReader);
		String whitespacePattern = "^\\s.*";
		String lastProperty = null;
		String line;

		while ((line = reader.readLine()) != null) {
			if (line.matches(whitespacePattern)) {
				if (lastProperty != null) {
					setMetadata(lastProperty, getValue(lastProperty)
							+ line.replaceFirst(whitespacePattern, " "));
				}
			}
			else {
				String[] parts = line.split(": ");

				if (parts.length == 2) {
					lastProperty = parts[0].trim();
					setMetadata(lastProperty, parts[1]);
				}
				else {
					// won't throw exception here, postpone to validation

					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("bagit.invalid_baginfo", new String[] {
								aBagInfoFile.getAbsolutePath(), line });
					}
				}
			}
		}
	}

	void writeTo(File aBagInfoFile) throws IOException {
		BufferedFileWriter writer = new BufferedFileWriter(aBagInfoFile);
		Iterator<String> iterator = getTags();

		while (iterator.hasNext()) {
			String tag = iterator.next();
			String value = myProperties.getProperty(tag);

			writer.append(tag).append(": ").append(value);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.debug.metadata"), new String[] {
						tag, value });
			}

			writer.newLine();
		}

		writer.close();
	}
}
