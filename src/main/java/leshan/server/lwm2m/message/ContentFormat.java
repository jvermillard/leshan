package leshan.server.lwm2m.message;

/**
 * Data format defined by the LWM2M specification
 */
public enum ContentFormat {

    LINK("application/link-format"), TEXT("application/vnd.oma.lwm2m+text"), TLV("application/vnd.oma.lwm2m+tlv"),
    JSON("application/vnd.oma.lwm2m+json"), OPAQUE("application/vnd.oma.lwm2m+opaque");

    private final String mediaType;

    private ContentFormat(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    /**
     * Find the {@link ContentFormat} for the given media type (<code>null</code> if not found)
     */
    public static ContentFormat fromMediaType(String mediaType) {
        for (ContentFormat t : ContentFormat.values()) {
            if (t.getMediaType() == mediaType) {
                return t;
            }
        }
        return null;
    }

}
