/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
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
package leshan.client.lwm2m.util;

import static org.eclipse.californium.core.coap.LinkFormat.*;

import java.util.Map;

import leshan.server.lwm2m.client.LinkObject;

public class LinkFormatUtils {
    public static final String INVALID_LINK_PAYLOAD = "<>";

    private static final String TRAILER = ", ";

    public static String payloadize(final LinkObject... linkObjects) {
        try {
            final StringBuilder builder = new StringBuilder();
            for (final LinkObject link : linkObjects) {
                builder.append(payloadizeLink(link)).append(TRAILER);
            }

            builder.delete(builder.length() - TRAILER.length(), builder.length());

            return builder.toString();
        } catch (final Exception e) {
            return INVALID_LINK_PAYLOAD;
        }
    }

    private static String payloadizeLink(final LinkObject link) {
        final StringBuilder builder = new StringBuilder();
        builder.append('<');
        builder.append(link.getUrl());
        builder.append('>');

        final Map<String, Object> attributes = link.getAttributes();

        if (hasPayloadAttributes(attributes)) {
            builder.append(";");
            if (attributes.containsKey(RESOURCE_TYPE)) {
                builder.append(RESOURCE_TYPE).append("=\"").append(attributes.get(RESOURCE_TYPE)).append("\"");
            }
            if (attributes.containsKey(INTERFACE_DESCRIPTION)) {
                builder.append(INTERFACE_DESCRIPTION).append("=\"").append(attributes.get(INTERFACE_DESCRIPTION))
                        .append("\"");
            }
            if (attributes.containsKey(CONTENT_TYPE)) {
                builder.append(CONTENT_TYPE).append("=\"").append(attributes.get(CONTENT_TYPE)).append("\"");
            }
            if (attributes.containsKey(MAX_SIZE_ESTIMATE)) {
                builder.append(MAX_SIZE_ESTIMATE).append("=\"").append(attributes.get(MAX_SIZE_ESTIMATE)).append("\"");
            }
            if (attributes.containsKey(OBSERVABLE)) {
                builder.append(OBSERVABLE);
            }
        }

        return builder.toString();
    }

    private static boolean hasPayloadAttributes(final Map<String, Object> attributes) {
        return attributes.containsKey(RESOURCE_TYPE) || //
                attributes.containsKey(INTERFACE_DESCRIPTION) || //
                attributes.containsKey(CONTENT_TYPE) || //
                attributes.containsKey(MAX_SIZE_ESTIMATE) || //
                attributes.containsKey(OBSERVABLE);
    }
}