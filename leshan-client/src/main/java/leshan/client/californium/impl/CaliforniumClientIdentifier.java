/*
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
package leshan.client.californium.impl;

import java.util.Objects;

import leshan.client.request.identifier.ClientIdentifier;

import org.eclipse.californium.core.coap.Request;

public class CaliforniumClientIdentifier implements ClientIdentifier {

    private final String location;
    private final String endpointIdentifier;

    public CaliforniumClientIdentifier(final String location, final String endpointIdentifier) {
        this.location = location;
        this.endpointIdentifier = endpointIdentifier;
    }

    public String getLocation() {
        return location;
    }

    public String getEndpointIdentifier() {
        return endpointIdentifier;
    }

    @Override
    public void accept(final Request coapRequest) {
        final String[] locationPaths = location.split("/");
        for (final String location : locationPaths) {
            if (location.length() != 0) {
                coapRequest.getOptions().addUriPath(location);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ClientIdentifier[").append(getEndpointIdentifier()).append("|").append(getLocation())
                .append("]");

        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointIdentifier, location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CaliforniumClientIdentifier)) {
            return false;
        }

        CaliforniumClientIdentifier other = (CaliforniumClientIdentifier) obj;

        boolean isEqual = true;
        isEqual = isEqual && Objects.equals(location, other.getLocation());
        isEqual = isEqual && Objects.equals(endpointIdentifier, other.getEndpointIdentifier());

        return isEqual;
    }
}
