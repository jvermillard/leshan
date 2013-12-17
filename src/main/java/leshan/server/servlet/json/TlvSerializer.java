/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
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
