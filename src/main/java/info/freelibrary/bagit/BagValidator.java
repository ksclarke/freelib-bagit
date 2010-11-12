package info.freelibrary.bagit;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.RegexFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagValidator extends I18nObject {

		private static final Logger LOGGER = LoggerFactory.getLogger(BagValidator.class);
	
		public boolean isComplete(Bag aBag) throws IOException {
			try {
				checkStructure(aBag);
			}
			catch (BagException details) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(getI18n("bagit.not_complete", new String[] {
							aBag.myDir.getName(), details.getMessage() }),
							details);
				}
				
				return false;
			}
			
			return true;
		}

		public boolean isValid(Bag aBag) throws IOException {
			try {
				validate(aBag);
				return true;
			}
			catch (BagException details) {
				return false;
			}
		}
		
		public ValidatedBag validate(Bag aBag) throws BagException, IOException {
			checkStructure(aBag);
			// check validity
			return new ValidatedBag(aBag);
		}

		private void checkStructure(Bag aBag) throws BagException, IOException {
			// check structure
		}
}
