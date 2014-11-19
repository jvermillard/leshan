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
package leshan.core.objectspec.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import leshan.core.objectspec.ObjectSpec;
import leshan.core.objectspec.ResourceSpec;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ObjectSpecDeserializer implements JsonDeserializer<ObjectSpec> {

    @Override
    public ObjectSpec deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null)
            return null;

        if (!json.isJsonObject())
            return null;

        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.has("id"))
            return null;

        int id = jsonObject.get("id").getAsInt();
        String name = jsonObject.get("name").getAsString();
        String instancetype = jsonObject.get("instancetype").getAsString();
        boolean mandatory = jsonObject.get("mandatory").getAsBoolean();
        String description = jsonObject.get("description").getAsString();
        ResourceSpec[] resourceSpecs = context.deserialize(jsonObject.get("resourcedefs"), ResourceSpec[].class);
        Map<Integer, ResourceSpec> resources = new HashMap<Integer, ResourceSpec>();
        for (ResourceSpec resourceSpec : resourceSpecs) {
            resources.put(resourceSpec.id, resourceSpec);
        }

        return new ObjectSpec(id, name, description, "multiple".equals(instancetype), mandatory, resources);
    }
}
