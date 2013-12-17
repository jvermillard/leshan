package leshan.server.lwm2m.message;

import java.util.Arrays;

import leshan.server.lwm2m.tlv.Tlv;

import org.apache.commons.lang.Validate;

/**
 * The request to change the value of a Resource, an array of Resources Instances or multiple Resources from an Object
 * Instance.
 */
public class WriteRequest {

    private final Integer objectId;

    private final Integer objectInstanceId;

    private final Integer resourceId;

    private final ContentFormat format;

    /** value for text and json content */
    private final String stringValue;

    /** value for TLV content */
    private final Tlv[] tlvValues;

    public WriteRequest(Integer objectId, Integer objectInstanceId, Integer resourceId, ContentFormat format,
            String stringValue, Tlv[] tlvValues) {
        Validate.notNull(objectId);
        Validate.notNull(objectInstanceId);
        Validate.notNull(format);

        switch (format) {
        case TEXT:
            Validate.notNull(resourceId);
        case JSON:
            Validate.notNull(stringValue);
            if (tlvValues != null) {
                throw new IllegalArgumentException("a value with format " + format + " cannot contain Tlv values");
            }
            break;
        case TLV:
            Validate.notNull(tlvValues);
            if (stringValue != null) {
                throw new IllegalArgumentException("a value with format TLV cannot contain a String value");
            }
        default:
            throw new IllegalArgumentException("unsupported content format for write request : " + format);
        }

        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = resourceId;
        this.format = format;
        this.stringValue = stringValue;
        this.tlvValues = tlvValues;

    }

    public Integer getObjectId() {
        return objectId;
    }

    public Integer getObjectInstanceId() {
        return objectInstanceId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public ContentFormat getFormat() {
        return format;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Tlv[] getTlvValues() {
        return tlvValues;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WriteRequest [objectId=").append(objectId).append(", objectInstanceId=")
                .append(objectInstanceId).append(", resourceId=").append(resourceId).append(", format=").append(format)
                .append(", stringValue=").append(stringValue).append(", tlvValues=").append(Arrays.toString(tlvValues))
                .append("]");
        return builder.toString();
    }

}
