
package info.freelibrary.bagit;

/**
 * Defines tags that may be used in <code>BagInfo</code>. Others may also be used and all are optional.
 */
public final class BagInfoTags {

    /**
     * Metadata tag for the organization transferring the content.
     */
    public static final String SOURCE_ORG = "Source-Organization";

    /**
     * Metadata tag for the mailing address of the organization.
     */
    public static final String ORG_ADDRESS = "Organization-Address";

    /**
     * Metadata tag for the person at the source organization who is responsible for the content transfer.
     */
    public static final String CONTACT_NAME = "Contact-Name";

    /**
     * Metadata tag for the international format telephone number of person or position responsible.
     */
    public static final String CONTACT_PHONE = "Contact-Phone";

    /**
     * Metadata tag for the fully qualified email address of the person or position responsible.
     */
    public static final String CONTACT_EMAIL = "Contact-Email";

    /**
     * Metadata tag for a brief explanation of the contents and provenance.
     */
    public static final String EXT_DESCRIPTION = "External-Description";

    /**
     * Metadata tag for the date (YYYY-MM-DD) that the content was prepared for delivery. This value is auto-generated
     * by default.
     */
    public static final String BAGGING_DATE = "Bagging-Date";

    /**
     * Metadata tag for the external identifier for the bag.
     */
    public static final String EXT_IDENTIFIER = "External-Identifier";

    /**
     * Metadata tag for the size or approximate size of the bag being transferred; bag size value should be followed
     * by an abbreviation such as MB, GB, or TB (e.g., 42600 MB, 43.6 GB, or .043 TB). Compared with Payload-Oxum, bag
     * size is intended for human consumption.
     */
    public static final String BAG_SIZE = "Bag-Size";

    /**
     * Metadata tag for the "octetstream sum" of the payload, namely, a two- part number of the form
     * "OctetCount.StreamCount", where OctetCount is the total number of octets (8-bit bytes) across all payload file
     * content and StreamCount is the total number of payload files. Payload-Oxum is easy to compute (e.g., on Unix
     * "wc -lc `find data/ -type f`") and should be included in "bag-info.txt" if at all possible. Compared to
     * Bag-Size (above), Payload-Oxum is intended for machine consumption. This value is auto-generated by default.
     */
    public static final String PAYLOAD_OXUM = "Payload-Oxum";

    /**
     * Metadata tag for the sender supplied identifier for the set; this identifier must be unique across the sender's
     * content, and if recognizable as belonging to a globally unique scheme, the receiver should make an effort to
     * honor reference to it.
     */
    public static final String BAG_GROUP_ID = "Bag-Group-Identifier";

    /**
     * Metadata tag for two numbers separated by "of", in particular, "N of T", where T is the total number of bags in
     * a group of bags and N is the ordinal number within the group; if T is not known, specify it as "?" (e.g., 1 of
     * 2, 4 of 4, 3 of ?).
     */
    public static final String BAG_COUNT = "Bag-Count";

    /**
     * Metadata tag for an alternate sender-specific identifier for the content and/or bag.
     */
    public static final String SENDER_IDENTIFIER = "Internal-Sender-Identifier";

    /**
     * Metadata tag for a sender-local prose description of the contents of the bag.
     */
    public static final String SENDER_DESCRIPTION = "Internal-Sender-Description";

    /**
     * Creates a BagInfoTags object.
     */
    private BagInfoTags() {
        // Utility classes shouldn't have a public constructor
    }
}