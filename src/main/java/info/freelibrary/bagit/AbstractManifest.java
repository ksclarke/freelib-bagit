
package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.freelibrary.util.BufferedFileReader;
import info.freelibrary.util.BufferedFileWriter;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.StringUtils;

/**
 * An abstract manifest class from which other specific manifests are implemented.
 */
abstract class AbstractManifest {

    private static final String DEFAULT_ALGORITHM = "md5";

    private static final String HASH_PROP = "bagit_hash";

    private static final String SPACE = " ";

    private final File myBagDir;

    private String myFileName;

    private String myHashAlgorithm;

    private final List<Entry> myHashes;

    private boolean myManifestsAreSortable;

    protected AbstractManifest(final File aBagDir) throws IOException {
        String pattern = StringUtils.format(getNamePattern(), "(.*)");
        final File[] files = aBagDir.listFiles(new RegexFileFilter(pattern));

        myHashes = new ArrayList<>();
        myBagDir = aBagDir;

        // Bag can contain more than one manifest, but must not contain more
        // than one of the same algorithm type - TODO: handle 2+ algorithms?
        if (files.length > 0) {
            final String fileName = files[0].getName();
            final Matcher matcher = Pattern.compile(pattern).matcher(fileName);
            BufferedFileReader reader = null;
            String line;

            try {
                reader = new BufferedFileReader(files[0]);

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(MessageCodes.BAGIT_002, fileName);
                }

                matcher.find();
                myHashAlgorithm = matcher.group(1);

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(MessageCodes.BAGIT_001, myHashAlgorithm);
                }

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(MessageCodes.BAGIT_034, files[0]);
                }

                while ((line = reader.readLine()) != null) {
                    final String cleanedLine = line.replaceAll("[\\s|\\*]+", SPACE);
                    final String[] parts = cleanedLine.split(SPACE);
                    final File file = new File(aBagDir, parts[1]);
                    final String relativePath = getRelativePath(file);

                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug(MessageCodes.BAGIT_035, parts[0] + SPACE + relativePath);
                    }

                    myHashes.add(new Entry(file, parts[0]));
                }
            } finally {
                reader.close();
            }
        } else {
            pattern = getNamePattern();
            myHashAlgorithm = System.getProperty(HASH_PROP, DEFAULT_ALGORITHM);
            myFileName = StringUtils.format(pattern, myHashAlgorithm);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug(MessageCodes.BAGIT_001, myHashAlgorithm);
            }

            myManifestsAreSortable = true;
        }
    }

    /**
     * Returns the hash algorithm used in the manifest file.
     *
     * @return The hash algorithm in the manifest file
     */
    public String getHashAlgorithm() {
        return myHashAlgorithm;
    }

    /**
     * Returns the number of entries in the manifest file.
     *
     * @return The number of entries in the manifest file
     */
    public int countEntries() {
        return myHashes.size();
    }

    @Override
    public String toString() {
        final String eol = System.getProperty("line.separator");
        final StringBuilder builder = new StringBuilder(50);
        final Iterator<Entry> iterator = myHashes.iterator();

        while (iterator.hasNext()) {
            final Entry entry = iterator.next();

            builder.append("<file name=\"");
            builder.append(getRelativePath(entry.myFile)).append("\" hash=\"");
            builder.append(entry.myHash).append("\" algorithm=\"");
            builder.append(myHashAlgorithm).append("\" />").append(eol);
        }

        return builder.toString();
    }

    /**
     * Gets the path of the supplied <code>File</code>, relative to the BagIt working directory.
     *
     * @param aFile A file to get the relative path of (relative to the working directory)
     */
    private String getRelativePath(final File aFile) {
        final int start = myBagDir.getAbsolutePath().length() + 1;
        return aFile.getAbsolutePath().substring(start);
    }

    protected abstract Logger getLogger();

    protected abstract String getNamePattern();

    File[] getFiles() {
        final ArrayList<File> files = new ArrayList<>();
        final Iterator<Entry> entryIterator = myHashes.iterator();

        while (entryIterator.hasNext()) {
            final Entry entry = entryIterator.next();
            files.add(entry.myFile);
        }

        return files.toArray(new File[myHashes.size()]);
    }

    /**
     * Returns the hash associated with the supplied <code>File</code> or null if there isn't one yet.
     *
     * @param aFile
     * @return
     */
    String getStoredHash(final File aFile) {
        final Iterator<Entry> entryIterator = myHashes.iterator();

        while (entryIterator.hasNext()) {
            final Entry entry = entryIterator.next();

            if (entry.myFile.getAbsolutePath().equals(aFile.getAbsolutePath())) {
                return entry.myHash;
            }
        }

        return null;
    }

    /**
     * Adds a new file to the manifest.
     *
     * @param aFile
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    void add(final File aFile) throws IOException, NoSuchAlgorithmException {
        myHashes.add(new Entry(aFile, FileUtils.hash(aFile, myHashAlgorithm)));
    }

    /**
     * Deletes the manifest file(s).
     *
     * @throws IOException If there is trouble deleting the manifest file(s)
     */
    void delete() throws IOException {
        final String pattern = StringUtils.format("[.*]", getNamePattern());
        final RegexFileFilter mFilter = new RegexFileFilter(pattern);

        // Remove the old payload file manifest if it exists
        for (final File manifest : myBagDir.listFiles(mFilter)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(MessageCodes.BAGIT_031, manifest);
            }

            if (!manifest.delete()) {
                throw new IOException(getLogger().getMessage(MessageCodes.BAGIT_006, manifest));
            }
        }

        myHashes.clear();
    }

    boolean remove(final File aFile) throws IOException, NoSuchAlgorithmException {
        final String algorithm = FileUtils.hash(aFile, myHashAlgorithm);
        return myHashes.remove(new Entry(aFile, algorithm));
    }

    /**
     * Writes the contents of the manifest to a file in the BagIt working directory.
     *
     * @throws IOException If there is a problem writing the manifest file
     */
    void writeToFile() throws IOException {
        final File manifestFile = new File(myBagDir, myFileName);
        final Iterator<Entry> iterator;

        BufferedFileWriter writer = null;

        if (myManifestsAreSortable) {
            Collections.sort(myHashes);
        }

        iterator = myHashes.iterator();

        try {
            if (manifestFile.exists()) {
                manifestFile.delete();
            }

            writer = new BufferedFileWriter(manifestFile);

            while (iterator.hasNext()) {
                final Entry entry = iterator.next();

                writer.write(entry.myHash);
                writer.write(SPACE);
                writer.write(getRelativePath(entry.myFile));
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static final class Entry implements Comparable<Entry> {

        private final File myFile;

        private final String myHash;

        private Entry(final File aFile, final String aHash) {
            myFile = aFile;
            myHash = aHash;
        }

        @Override
        public int compareTo(final Entry aEntry) {
            if (aEntry == this) {
                return 0;
            }

            return myFile.getAbsolutePath().compareToIgnoreCase(aEntry.myFile.getAbsolutePath());
        }

        @Override
        public boolean equals(final Object aObject) {
            if (aObject == this) {
                return true;
            }

            if (aObject instanceof Entry) {
                return myFile.getAbsolutePath().equals(((Entry) aObject).myFile.getAbsolutePath());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return myFile.getAbsolutePath().hashCode();
        }
    }
}
