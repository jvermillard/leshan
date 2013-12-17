package leshan.server.servlet.json;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentResponse;
import leshan.server.lwm2m.tlv.TlvDecoder;

import org.apache.commons.codec.binary.Hex;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ResponseSerializer implements JsonSerializer<ClientResponse> {

    private final TlvDecoder tlvDecoder = new TlvDecoder();

    @Override
    public JsonElement serialize(ClientResponse src, Type typeOfSrc, JsonSerializationContext context) {
        try {
            JsonObject element = new JsonObject();

            element.addProperty("status", src.getCode().toString());

            if (src instanceof ContentResponse) {
                Object value = null;
                ContentResponse cResponse = (ContentResponse) src;
                switch (cResponse.getFormat()) {
                case TLV:
                    value = tlvDecoder.decode(ByteBuffer.wrap(cResponse.getContent()));
                    break;
                case TEXT:
                case JSON:
                case LINK:
                    value = new String(cResponse.getContent(), "UTF-8");
                    break;
                case OPAQUE:
                    value = Hex.encodeHexString(cResponse.getContent());
                    break;
                }
                element.add("value", context.serialize(value));
            }

            return element;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
