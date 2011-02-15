package info.freelibrary.bagit;

import info.freelibrary.util.BufferedFileWriter;
import info.freelibrary.util.I18nObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metadata elements describing the bag and payload. The metadata elements, all
 * optional, are intended primarily for human readability.
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
public class BagInfo extends I18nObject implements BagInfoConstants, Cloneable {

	private static final Logger LOGGER = LoggerFactory.getLogger(BagInfo.class);

	static final String FILE_NAME = "bag-info.txt";
	
	private static class Metadata implements Cloneable {

		private String myTag;
		private String myValue;

		private Metadata(String aTag, String aValue) {
			if (aTag == null || aValue == null) {
				throw new NullPointerException();
			}

			myTag = aTag;
			myValue = aValue;
		}

		public Metadata clone() {
			return new Metadata(myTag, myValue);
		}
	}

	private ArrayList<Metadata> myMetadata;

	/**
	 * Whether the bag-info has been validated or not yet.
	 */
	boolean isValid;

	/**
	 * Creates a new <code>BagInfo</code> for <code>Bag</code> metadata.
	 */
	public BagInfo() {
		myMetadata = new ArrayList<Metadata>();
	}

	/**
	 * Creates a new <code>BagInfo</code> from a Java <code>Properties</code>
	 * object.
	 * 
	 * @param aProperties A Java <code>Properties</code> from which to build the
	 *        <code>BagInfo</code>
	 */
	public BagInfo(Properties aProperties) {
		Iterator<String> props = aProperties.stringPropertyNames().iterator();
		
		myMetadata = new ArrayList<Metadata>();

		while (props.hasNext()) {
			String name = props.next();
			String value = aProperties.getProperty(name);

			if (!value.equals("")) {
				myMetadata.add(new Metadata(name, value));
			}
		}
	}

	public BagInfo(BagInfo aBagInfo) {
		Iterator<Metadata> iterator = aBagInfo.myMetadata.iterator();
		
		myMetadata = new ArrayList<Metadata>();
		
		while (iterator.hasNext()) {
			Metadata metadata = iterator.next();
			myMetadata.add(metadata.clone());
		}
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
		myMetadata = new ArrayList<Metadata>();

		if (bagInfo.exists()) {
			readFrom(bagInfo);
		}
		else {
			if (!bagInfo.createNewFile()) {
				throw new IOException(getI18n("bagit.baginfo_create", bagInfo));
			}
		}
	}

	/**
	 * Creates a new <code>BagInfo</code> from this one.
	 */
	public BagInfo clone() throws CloneNotSupportedException {
		super.clone();
		return new BagInfo(this);
	}

	/**
	 * Tests whether the <code>BagInfo</code> contains the supplied tag.
	 * 
	 * @param aTag A tag to test for occurrence
	 * @return True if the supplied tag can be found; else, false
	 */
	public boolean containsTag(String aTag) {
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
	 * @return The number of tags found in tihs <code>BagInfo</code>
	 */
	public int countTags() {
		return myMetadata.size();
	}

	/**
	 * Returns an array of tags found in this <code>BagInfo</code>.
	 * 
	 * @return An array of tags
	 */
	public String[] getTags() {
		String[] tags = new String[myMetadata.size()];

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
	public String getValue(String aTag) {
		for (int index = 0; index < myMetadata.size(); index++) {
			Metadata metadata = myMetadata.get(index);

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
	public int countTags(String aTag) {
		return getValues(aTag).length;
	}

	/**
	 * Returns all the values for the supplied tag or an empty array if the tag
	 * isn't found.
	 * 
	 * @param aTag The tag whose value we want to learn
	 * @return The value for the supplied tag
	 */
	public String[] getValues(String aTag) {
		ArrayList<String> matches = new ArrayList<String>();

		for (int index = 0; index < myMetadata.size(); index++) {
			Metadata metadata = myMetadata.get(index);

			if (aTag.equals(metadata.myTag)) {
				matches.add(metadata.myValue);
			}
		}

		return matches.toArray(new String[matches.size()]);
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
		String value = getValue(aTag);
		return value == null ? aDefaultValue : value;
	}

	/**
	 * Returns the value of the metadata element at the supplied index position.
	 * 
	 * @param aIndex The index position from which we want the value
	 * @return The value of the metadata element at the supplied index position
	 */
	public String getValue(int aIndex) {
		return myMetadata.get(aIndex).myValue;
	}

	/**
	 * Returns the tag of the metadata element at the supplied index position.
	 * 
	 * @param aIndex The index position from which we want the tag
	 * @return The tag of the metadata element at the supplied index position
	 */
	public String getTag(int aIndex) {
		return myMetadata.get(aIndex).myTag;
	}

	/**
	 * Removes all metadata elements that match the supplied tag.
	 * 
	 * @param aTag The tag whose metadata element we want to remove
	 * @return True if the metadata elements were successfully removed; else,
	 *         false
	 */
	public boolean removeMetadata(String aTag) {
		int totalCount = myMetadata.size();

		if (isValid) {
			throw new RuntimeException(getI18n("bagit.validated"));
		}

		for (int index = 0; index < totalCount; index++) {
			Metadata metadata = myMetadata.get(index);

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
	 */
	public boolean addMetadata(String aTag, String aValue) {
		if (isValid) {
			throw new RuntimeException(getI18n("bagit.validated"));
		}

		return myMetadata.add(new Metadata(aTag, aValue));
	}

	/**
	 * Outputs an XML representation of the <code>BagInfo</code>, useful for
	 * debugging.
	 * 
	 * @return An XML representation of the <code>BagInfo</code>
	 */
	public String toString() {
		String eol = System.getProperty("line.separator");
		Iterator<Metadata> iterator = myMetadata.iterator();
		StringBuilder builder = new StringBuilder();

		builder.append("<bagInfo>").append(eol);

		while (iterator.hasNext()) {
			Metadata metadata = iterator.next();

			builder.append("<metadata tag=\"").append(metadata.myTag);
			builder.append("\" value=\"").append(metadata.myValue);
			builder.append("\" />").append(eol);
		}

		builder.append("</bagInfo>").append(eol);

		return builder.toString();
	}

	private void readFrom(File aBagInfoFile) throws IOException {
		FileReader fileReader = new FileReader(aBagInfoFile);
		BufferedReader reader = new BufferedReader(fileReader);
		String whitespacePattern = "^\\s.*";
		String lastProp = null;
		String line;

		try {
			while ((line = reader.readLine()) != null) {
				if (line.matches(whitespacePattern)) {
					if (lastProp != null) {
						line = line.replaceFirst(whitespacePattern, " ");
						addMetadata(lastProp, getValue(lastProp) + line);
					}
				}
				else {
					String[] parts = line.split(": ");

					if (parts.length == 2) {
						lastProp = parts[0].trim();
						addMetadata(lastProp, parts[1]);
					}
					else {
						// don't throw exception here; postpone to validation
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("bagit.invalid_baginfo", new String[] {
									aBagInfoFile.getAbsolutePath(), line });
						}
					}
				}
			}
		}
		finally {
			reader.close();
		}
	}

	void writeTo(File aBagInfoFile) throws IOException {
		BufferedFileWriter writer = new BufferedFileWriter(aBagInfoFile);

		try {
			for (int index = 0; index < myMetadata.size(); index++) {
				Metadata metadata = myMetadata.get(index);

				writer.append(metadata.myTag).append(": ");
				writer.append(metadata.myValue);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(getI18n("bagit.debug.metadata"), new String[] {
							metadata.myTag, metadata.myValue });
				}

				writer.newLine();
			}
		}
		finally {
			writer.close();
		}
	}
}
