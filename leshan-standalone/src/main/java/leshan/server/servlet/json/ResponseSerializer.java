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
