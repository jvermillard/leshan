package leshan.server.lwm2m.message.server;

import java.util.Arrays;

import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.tlv.Tlv;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.mina.coap.CoapMessage;

/**
 * The request to change the value of a Resource, an array of Resources Instances or multiple Resources from an Object
 * Instance.
 */
public class WriteRequest implements ServerRequest {

    private final int id;

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

        this.id = RandomUtils.nextInt() & 0xFFFF;

        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = resourceId;
        this.format = format;
        this.stringValue = stringValue;
        this.tlvValues = tlvValues;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoapMessage encode(MessageEncoder visitor) {
        return visitor.encode(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object requestId() {
        return id;
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
        builder.append("WriteRequest [id=").append(id).append(", objectId=").append(objectId)
                .append(", objectInstanceId=").append(objectInstanceId).append(", resourceId=").append(resourceId)
                .append(", format=").append(format).append(", stringValue=").append(stringValue).append(", tlvValues=")
                .append(Arrays.toString(tlvValues)).append("]");
        return builder.toString();
    }

}
