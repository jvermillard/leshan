package leshan.server.servlet.json;

import java.lang.reflect.Type;

import leshan.server.lwm2m.client.Client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ClientSerializer implements JsonSerializer<Client> {

    @Override
    public JsonElement serialize(Client src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject element = new JsonObject();

        element.addProperty("endpoint", src.getEndpoint());
        element.addProperty("registrationId", src.getRegistrationId());
        element.add("registrationDate", context.serialize(src.getRegistrationDate()));
        element.addProperty("address", src.getAddress().toString() + ":" + src.getPort());
        element.addProperty("smsNumber", src.getSmsNumber());
        element.addProperty("lwM2MmVersion", src.getLwM2mVersion());
        element.addProperty("lifetime", src.getLifeTimeInSec());
        element.add("objectLinks", context.serialize(src.getObjectLinks()));

        return element;
    }
}
