
package info.freelibrary.bagit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;

import info.freelibrary.util.BufferedFileWriter;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * A bag's required <code>BagIt</code> declaration.
 */
class Declaration {

    public static final String VERSION = "0.96";

    private static final String ENCODING_TAG = "Tag-File-Character-Encoding";

    private static final String FILE_NAME = "bagit.txt";

    private static final String VERSION_TAG = "BagIt-Version";

    private static final String METADATA_DELIM = ": ";

    private static final Logger LOGGER = LoggerFactory.getLogger(Declaration.class, Constants.BUNDLE_NAME);

    private final boolean isValid;

    private final File myBagDir;

    private String myEncoding;

    private String myVersion;

    Declaration(final File aBagDir) throws IOException {
        this(aBagDir, true);
    }

    /**
     * The tag file required to be in all bags conforming to this specification. Contains tags necessary for
     * bootstrapping the reading and processing of the rest of a bag.
     *
     * @param aBagDir The working directory of the declaration's <code>Bag</code>
     * @throws IOException If there is trouble writing the <code>BagItDeclaration</code>
     */
    Declaration(final File aBagDir, final boolean aExisting) throws IOException {
        myBagDir = aBagDir;

        if (aExisting) {
            final File bagItTxt = new File(aBagDir, FILE_NAME);

            if (!bagItTxt.exists()) {
                throw new FileNotFoundException(bagItTxt.getAbsolutePath());
            } else {
                final FileReader fileReader = new FileReader(bagItTxt);
                LineNumberReader reader = null;
                boolean validity = true;
                String line;

                try {
                    reader = new LineNumberReader(new BufferedReader(fileReader));

                    while ((line = reader.readLine()) != null) {
                        final int start = line.indexOf(':') + 1;

                        if (reader.getLineNumber() == 1 && line.startsWith(VERSION_TAG + METADATA_DELIM)) {
                            myVersion = line.substring(start).trim();
                        } else if (reader.getLineNumber() == 2 && line.startsWith(ENCODING_TAG + METADATA_DELIM)) {
                            myEncoding = line.substring(start).trim();
                        } else {
                            // Spec says MUST consist of only two lines (above)
                            validity = false;
                        }
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }

                isValid = validity;
            }
        } else {
            myVersion = VERSION;
            myEncoding = StandardCharsets.UTF_8.toString();
            isValid = true;
        }
    }

    /**
     * Gets the character encoding used in this <code>Bag</code>, UTF-8.
     *
     * @return The character encoding used in this <code>Bag</code>, UTF-8
     */
    String getEncoding() {
        return myEncoding == null ? StandardCharsets.UTF_8.toString() : myEncoding;
    }

    /**
     * Gets the BagIt specification version to which this declaration conforms.
     *
     * @return The BagIt specification version
     */
    String getVersion() {
        return myVersion == null ? VERSION : myVersion;
    }

    /**
     * Attempts to validate this BagIt declaration. It throws a <code>BagException</code> if there is a problem with
     * it.
     *
     * @throws BagException A problem preventing the declaration from being a valid BagIt declaration
     */
    void validate() throws BagException, IOException {
        if (new File(myBagDir, FILE_NAME).exists()) {
            if (!isValid) {
                throw new BagException(MessageCodes.BAGIT_013);
            }

            if (myVersion == null) {
                throw new BagException(MessageCodes.BAGIT_009);
            }

            if (myVersion.indexOf('.') == -1) {
                throw new BagException(MessageCodes.BAGIT_010);
            }

            if (myEncoding == null) {
                throw new BagException(MessageCodes.BAGIT_011);
            }

            if (!StandardCharsets.UTF_8.toString().equals(myEncoding)) {
                throw new BagException(MessageCodes.BAGIT_012);
            }
        } else {
            writeToFile(); // We shouldn't be able to write an invalid file
        }
    }

    /**
     * Returns an XML representation of the BagIt declaration.
     *
     * @return An XML representation of the BagIt declaration
     */
    @Override
    public String toString() {
        final String eol = System.getProperty("line.separator");
        final StringBuilder builder = new StringBuilder(70);

        builder.append("<declaration>").append(eol).append("<version>").append(getVersion()).append("</version>")
                .append(eol).append("<encoding>").append(getEncoding()).append("</encoding>").append(eol).append(
                        "</declaration>").append(eol);

        return builder.toString();
    }

    /**
     * Writes a new <code>bagit.txt</code> file. If you are working with an existing bag that already has a valid
     * declaration, you don't need to write a new one. This method uses this library's defaults and will overwrite a
     * declaration from another bagit library.
     *
     * @throws IOException If there is trouble writing the <code>bagit.txt</code> file
     */
    void writeToFile() throws IOException {
        final File bagItTxt = new File(myBagDir, FILE_NAME);

        if (bagItTxt.exists() && !bagItTxt.delete()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.BAGIT_006, bagItTxt));
        }

        BufferedFileWriter writer = null;

        try {
            writer = new BufferedFileWriter(bagItTxt);
            writer.write(VERSION_TAG + METADATA_DELIM + VERSION);
            writer.newLine();
            writer.write(ENCODING_TAG + METADATA_DELIM + StandardCharsets.UTF_8.toString());
            writer.newLine();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
