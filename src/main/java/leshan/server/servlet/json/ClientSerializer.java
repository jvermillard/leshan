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
