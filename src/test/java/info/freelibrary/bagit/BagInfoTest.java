package info.freelibrary.bagit;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import info.freelibrary.util.I18nObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagInfoTest extends I18nObject implements BagInfoConstants {

	private static final String EOL = System.getProperty("line.separator");

	private static final String DIR_PATH = "src/test/resources/bagInfo/";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BagInfoTest.class);

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		new File(DIR_PATH).delete();
	}
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		new File(DIR_PATH).mkdirs();
	}
	
	@Test
	public void testBagInfoProperties() {
		Properties properties = new Properties();
		BagInfo bagInfo;

		properties.setProperty(CONTACT_EMAIL_TAG, "ksclarke@gmail.com");
		properties.setProperty(CONTACT_NAME_TAG, "Kevin S. Clarke");
		properties.setProperty(SOURCE_ORG_TAG, "FreeLibrary.INFO");

		bagInfo = new BagInfo(properties);

		assertEquals(bagInfo.getValue(CONTACT_EMAIL_TAG), "ksclarke@gmail.com");
		assertEquals(bagInfo.getValue(CONTACT_NAME_TAG), "Kevin S. Clarke");
		assertEquals(bagInfo.getValue(SOURCE_ORG_TAG), "FreeLibrary.INFO");

		assertEquals(3, bagInfo.countTags());
	}

	@Test
	public void testSetMetadata() {
		BagInfo bagInfo = new BagInfo();

		bagInfo.setMetadata(CONTACT_EMAIL_TAG, "ksclarke@gmail.com");
		bagInfo.setMetadata(CONTACT_NAME_TAG, "Kevin S. Clarke");

		assertEquals(bagInfo.getValue(CONTACT_EMAIL_TAG), "ksclarke@gmail.com");
		assertEquals(bagInfo.getValue(CONTACT_NAME_TAG), "Kevin S. Clarke");
		assertEquals(bagInfo.countTags(), 2);
	}

	@Test
	public void testGetValueString() {
		String emailAddress = "ksclarke@gmail.com";
		BagInfo bagInfo = new BagInfo();

		bagInfo.setMetadata(CONTACT_EMAIL_TAG, emailAddress);
		assertEquals(emailAddress, bagInfo.getValue(CONTACT_EMAIL_TAG));
	}

	@Test
	public void testRemoveMetadata() {
		BagInfo bagInfo = new BagInfo();
		bagInfo.setMetadata(CONTACT_EMAIL_TAG, "ksclarke@gmail.com");
		assertEquals(bagInfo.countTags(), 1);
		bagInfo.removeMetadata(CONTACT_EMAIL_TAG);
		assertEquals(bagInfo.countTags(), 0);
	}

	@Test
	public void testGetValueStringString() {
		String emailAddress1 = "ksclarke@gmail.com";
		String emailAddress2 = "thetrashcan@gmail.com";
		BagInfo bagInfo = new BagInfo();
		String result;

		bagInfo.setMetadata(CONTACT_EMAIL_TAG, emailAddress1);
		result = bagInfo.getValue(CONTACT_EMAIL_TAG, emailAddress2);
		assertEquals(result, emailAddress1);

		bagInfo.removeMetadata(CONTACT_EMAIL_TAG);
		result = bagInfo.getValue(CONTACT_EMAIL_TAG, emailAddress2);
		assertEquals(result, emailAddress2);
	}

	@Test
	public void testGetTags() {
		BagInfo bagInfo = new BagInfo();

		bagInfo.setMetadata(CONTACT_EMAIL_TAG, "ksclarke@gmail.com");
		bagInfo.setMetadata(CONTACT_NAME_TAG, "Kevin S. Clarke");
		bagInfo.setMetadata(ORG_ADDRESS_TAG, "FreeLibrary.INFO");
		assertEquals(3, bagInfo.countTags());

		Iterator<String> iterator = bagInfo.getTags();
		int count = 0;

		while (iterator.hasNext()) {
			String tag = iterator.next();

			if (!bagInfo.containsTag(tag)) {
				fail("Huh? Found something we don't think we have");
			}

			count += 1;
		}

		assertEquals(3, count);
	}

	@Test
	public void testWriteToFile() {
		File testBagInfo = new File(DIR_PATH + "bag-info.txt");
		BagInfo bagInfo = new BagInfo();
		bagInfo.setMetadata(CONTACT_EMAIL_TAG, "ksclarke@gmail.com");
		bagInfo.setMetadata(CONTACT_NAME_TAG, "Kevin S. Clarke");
		bagInfo.setMetadata(ORG_ADDRESS_TAG, "FreeLibrary.INFO");

		try {
			bagInfo.writeTo(testBagInfo);
		}
		catch (IOException details) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(details.getMessage(), details);
			}

			fail(details.getMessage());
		}

		try {
			FileReader fileReader = new FileReader(testBagInfo);
			BufferedReader reader = new BufferedReader(fileReader);
			StringBuilder buffer = new StringBuilder();
			String expected = new String();
			String line;

			while ((line = reader.readLine()) != null) {
				buffer.append(line).append(EOL);
			}

			expected += "Organization-Address: FreeLibrary.INFO" + EOL;
			expected += "Contact-Email: ksclarke@gmail.com" + EOL;
			expected += "Contact-Name: Kevin S. Clarke" + EOL;

			assertEquals(expected.toString(), buffer.toString());
		}
		catch (IOException details) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(details.getMessage(), details);
			}

			fail(details.getMessage());
		}
	}

	@Test
	public void testReadFrom() {
		try {
			File testBagInfo = new File(DIR_PATH + "bag-info.txt");
			BagInfo bagInfo = new BagInfo(testBagInfo.getParentFile());
			String contactName = bagInfo.getValue(CONTACT_NAME_TAG);
			String orgAddress = bagInfo.getValue(ORG_ADDRESS_TAG);
			String email = bagInfo.getValue(CONTACT_EMAIL_TAG);

			assertEquals("FreeLibrary.INFO", orgAddress);
			assertEquals("ksclarke@gmail.com", email);
			assertEquals("Kevin S. Clarke", contactName);
			assertEquals(3, bagInfo.countTags());

			if (!testBagInfo.delete()) {
				fail(getI18n("bagit.test.failed_baginfo_delete", testBagInfo));
			}
		}
		catch (IOException details) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(details.getMessage(), details);
			}
			
			fail(details.getMessage());
		}
	}

}
