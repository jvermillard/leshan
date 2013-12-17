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
package leshan.server.lwm2m.message;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

/**
 * A response with content from the LW-M2M client.
 */
public class ContentResponse extends ClientResponse {

    private final byte[] content;

    private final ContentFormat format;

    public ContentResponse(byte[] content, ContentFormat format) {
        super(ResponseCode.CONTENT);

        Validate.notNull(format);

        this.content = content;
        this.format = format;
    }

    public byte[] getContent() {
        return content;
    }

    public ContentFormat getFormat() {
        return format;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ContentResponse [content=").append(Arrays.toString(content)).append(", format=").append(format)
                .append("]");
        return builder.toString();
    }

}
