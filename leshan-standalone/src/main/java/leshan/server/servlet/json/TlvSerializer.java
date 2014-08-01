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
            element.addProperty("type", src.getType().toString());
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
