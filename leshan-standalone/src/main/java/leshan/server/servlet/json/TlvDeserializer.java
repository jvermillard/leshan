package leshan.server.servlet.json;

import java.lang.reflect.Type;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class TlvDeserializer implements JsonDeserializer<Tlv> {

    @Override
    public Tlv deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (json == null) {
            return null;
        }

        if (json.isJsonObject()) {
            JsonObject object = (JsonObject) json;

            // id
            int id;
            if (object.has("id")) {
                id = object.get("id").getAsInt();
            } else {
                throw new JsonParseException("Missing id");
            }

            // type
            TlvType tlvtype = null;
            if (object.has("type")) {
                String type = object.get("type").getAsString();
                tlvtype = TlvType.valueOf(type);
            } else {
                throw new JsonParseException("Missing type");
            }

            switch (tlvtype) {
            case RESOURCE_VALUE:
            case RESOURCE_INSTANCE:
                if (object.has("value")) {
                    // TODO manage long and date
                    JsonPrimitive jsonPrimitive = object.get("value").getAsJsonPrimitive();
                    if (jsonPrimitive.isString()) {
                        return Tlv.newStringValue(tlvtype, jsonPrimitive.getAsString(), id);
                    } else if (jsonPrimitive.isNumber()) {
                        return Tlv.newIntegerValue(tlvtype, jsonPrimitive.getAsInt(), id);

                    } else if (jsonPrimitive.isBoolean()) {
                        return Tlv.newBooleanValue(tlvtype, jsonPrimitive.getAsBoolean(), id);
                    }
                } else {
                    throw new JsonParseException("Missing value");
                }
                break;
            case OBJECT_INSTANCE:
            case MULTIPLE_RESOURCE:
                // TODO manage resources field (children)
                throw new JsonParseException("OBJECT_INSTANCE and MULTIPLE_RESOURCE not supported");
            }

        }
        return null;
    }
}
