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

import leshan.core.node.LwM2mNode;
import leshan.core.node.LwM2mObject;
import leshan.core.node.LwM2mObjectInstance;
import leshan.core.node.LwM2mResource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LwM2mNodeSerializer implements JsonSerializer<LwM2mNode> {

    @Override
    public JsonElement serialize(LwM2mNode src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject element = new JsonObject();

        element.addProperty("id", src.getId());

        if (typeOfSrc == LwM2mObject.class) {
            element.add("instances", context.serialize(((LwM2mObject) src).getInstances().values()));
        } else if (typeOfSrc == LwM2mObjectInstance.class) {
            element.add("resources", context.serialize(((LwM2mObjectInstance) src).getResources().values()));
        } else if (typeOfSrc == LwM2mResource.class) {
            LwM2mResource rsc = (LwM2mResource) src;
            if (rsc.isMultiInstances()) {
                Object[] values = new Object[rsc.getValues().length];
                for (int i = 0; i < rsc.getValues().length; i++) {
                    values[i] = rsc.getValues()[i].value;
                }
                element.add("values", context.serialize(values));
            } else {
                element.add("value", context.serialize(rsc.getValue().value));
            }
        }

        return element;
    }

}
