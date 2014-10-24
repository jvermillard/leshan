/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.standalone.servlet.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import leshan.server.node.LwM2mNode;
import leshan.server.node.LwM2mObject;
import leshan.server.node.LwM2mObjectInstance;
import leshan.server.node.LwM2mResource;
import leshan.server.node.Value;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class LwM2mNodeDeserializer implements JsonDeserializer<LwM2mNode> {

    @Override
    public LwM2mNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (json == null) {
            return null;
        }

        LwM2mNode node = null;

        if (json.isJsonObject()) {
            JsonObject object = (JsonObject) json;

            if (!object.has("id")) {
                throw new JsonParseException("Missing id");
            }
            int id = object.get("id").getAsInt();

            if (object.has("instances")) {

                JsonArray array = object.get("instances").getAsJsonArray();
                LwM2mObjectInstance[] instances = new LwM2mObjectInstance[array.size()];

                for (int i = 0; i < array.size(); i++) {
                    instances[i] = context.deserialize(array.get(i), LwM2mNode.class);
                }
                node = new LwM2mObject(id, instances);

            } else if (object.has("resources")) {
                JsonArray array = object.get("resources").getAsJsonArray();
                LwM2mResource[] resources = new LwM2mResource[array.size()];

                for (int i = 0; i < array.size(); i++) {
                    resources[i] = context.deserialize(array.get(i), LwM2mNode.class);
                }
                node = new LwM2mObjectInstance(id, resources);

            } else if (object.has("value")) {
                // single value resource
                node = new LwM2mResource(id, this.deserializeValue(object.get("value").getAsJsonPrimitive()));
            } else if (object.has("values")) {
                // multi-instances resource
                Collection<Value<?>> values = new ArrayList<>();
                for (JsonElement val : object.get("values").getAsJsonArray()) {
                    values.add(this.deserializeValue(val.getAsJsonPrimitive()));
                }
                node = new LwM2mResource(id, values.toArray(new Value<?>[0]));
            } else {
                throw new JsonParseException("Invalid node element");
            }
        } else {
            throw new JsonParseException("Invalid node element");
        }

        return node;
    }

    private Value<?> deserializeValue(JsonPrimitive val) {
        Value<?> value = null;
        if (val.isNumber()) {

            Number n = val.getAsNumber();
            if (n.doubleValue() == (long) n.doubleValue()) {
                Long lValue = Long.valueOf(n.longValue());
                if (lValue >= Integer.MIN_VALUE && lValue <= Integer.MAX_VALUE) {
                    value = Value.newIntegerValue(lValue.intValue());
                } else {
                    value = Value.newLongValue(lValue);
                }
            } else {
                Double dValue = Double.valueOf(n.doubleValue());
                if (dValue >= Float.MIN_VALUE && dValue <= Float.MAX_VALUE) {
                    value = Value.newFloatValue(dValue.floatValue());
                } else {
                    value = Value.newDoubleValue(dValue);
                }
            }

        } else if (val.isBoolean()) {
            value = Value.newBooleanValue(val.getAsBoolean());
        } else if (val.isString()) {
            value = Value.newStringValue(val.getAsString());
        }
        return value;
    }
}
