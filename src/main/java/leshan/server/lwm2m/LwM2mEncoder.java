package leshan.server.lwm2m;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.server.CreatedResponse;
import leshan.server.lwm2m.message.server.DeletedResponse;
import leshan.server.lwm2m.message.server.ErrorResponse;
import leshan.server.lwm2m.message.server.MessageEncoder;
import leshan.server.lwm2m.message.server.ReadRequest;
import leshan.server.lwm2m.message.server.WriteRequest;
import leshan.server.tlv.TlvEncoder;

import org.apache.commons.lang.NotImplementedException;
import org.apache.mina.coap.CoapCode;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.coap.CoapOption;
import org.apache.mina.coap.CoapOptionType;
import org.apache.mina.coap.MessageType;

public class LwM2mEncoder implements MessageEncoder {

    private final TlvEncoder tlvEncoder = new TlvEncoder();

    /**
     * {@inheritDoc}
     */
    @Override
    public CoapMessage encode(CreatedResponse message) {

        CoapOption[] options = new CoapOption[message.getNewLocation().length];
        for (int i = 0; i < message.getNewLocation().length; i++) {
            try {
                options[i] = new CoapOption(CoapOptionType.LOCATION_PATH, message.getNewLocation()[i].getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }

        return new CoapMessage(1, MessageType.ACK, ResponseCode.CREATED.getCoapCode(), message.getId(), null, options,
                null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoapMessage encode(ErrorResponse message) {
        return new CoapMessage(1, MessageType.ACK, message.getErrorCode().getCoapCode(), message.getId(), null, null,
                null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoapMessage encode(DeletedResponse message) {
        return new CoapMessage(1, MessageType.ACK, ResponseCode.DELETED.getCoapCode(), message.getId(), null, null,
                null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoapMessage encode(ReadRequest message) {
        List<CoapOption> options = new ArrayList<>();
        try {
            // objectId
            options.add(new CoapOption(CoapOptionType.URI_PATH, Integer.toString(message.getObjectId()).getBytes(
                    "UTF-8")));

            // objectInstanceId
            if (message.getObjectInstanceId() == null) {
                if (message.getResourceId() != null) {
                    options.add(new CoapOption(CoapOptionType.URI_PATH, "0".getBytes("UTF-8"))); // default instanceId
                }
            } else {
                options.add(new CoapOption(CoapOptionType.URI_PATH, Integer.toString(message.getObjectInstanceId())
                        .getBytes("UTF-8")));
            }

            // resourceId
            if (message.getResourceId() != null) {
                options.add(new CoapOption(CoapOptionType.URI_PATH, Integer.toString(message.getResourceId()).getBytes(
                        "UTF-8")));
            }

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        byte[] token = new byte[] {}; // empty token since a piggy-backed response is expected
        return new CoapMessage(1, MessageType.CONFIRMABLE, CoapCode.GET.getCode(), message.getId(), token,
                options.toArray(new CoapOption[0]), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoapMessage encode(WriteRequest message) {

        List<CoapOption> options = new ArrayList<>();
        byte[] payload = null;
        try {
            // objectId
            options.add(new CoapOption(CoapOptionType.URI_PATH, Integer.toString(message.getObjectId()).getBytes(
                    "UTF-8")));

            // objectInstanceId
            options.add(new CoapOption(CoapOptionType.URI_PATH, Integer.toString(message.getObjectInstanceId())
                    .getBytes("UTF-8")));

            // resourceId
            if (message.getResourceId() != null) {
                options.add(new CoapOption(CoapOptionType.URI_PATH, Integer.toString(message.getResourceId()).getBytes(
                        "UTF-8")));
            }

            // value
            switch (message.getFormat()) {
            case TEXT:
                payload = message.getStringValue().getBytes("UTF-8");
                break;
            case TLV:
                payload = tlvEncoder.encode(message.getTlvValues(), null).array();
                // TODO add an option for content type
                break;
            case JSON:
                throw new NotImplementedException("JSON not supported for write requests");
            default:
                throw new IllegalStateException("invalid format for write request : " + message.getFormat());
            }

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        byte[] token = new byte[] {}; // empty token since a piggy-backed response is expected
        return new CoapMessage(1, MessageType.CONFIRMABLE, CoapCode.PUT.getCode(), message.getId(), token,
                options.toArray(new CoapOption[0]), payload);
    }
}
