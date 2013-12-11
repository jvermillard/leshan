package leshan.server.servlet.json;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import leshan.server.lwm2m.tlv.Tlv;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * A custom JSON serializer for {@link Tlv} object.
 */
public class TlvSerializer implements JsonSerializer<Tlv> {

    @Override
    public JsonElement serialize(Tlv src, Type typeOfSrc, JsonSerializationContext context) {
        try {
            JsonObject element = new JsonObject();
            element.addProperty("id", src.getIdentifier());
            switch (src.getType()) {
            case RESOURCE_VALUE:
            case RESOURCE_INSTANCE:
                // value
                String value = new String(src.getValue(), "UTF-8");
                if (StringUtils.isAsciiPrintable(value)) {
                    element.addProperty("value", value);
                } else {
                    element.addProperty("value", "[Hex] " + Hex.encodeHexString(src.getValue()));
                }
                break;
            case OBJECT_INSTANCE:
            case MULTIPLE_RESOURCE:
                // children
                JsonArray children = new JsonArray();
                for (Tlv child : src.getChildren()) {
                    children.add(context.serialize(child));
                }
                element.add("resources", children);
                break;
            }
            return element;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
