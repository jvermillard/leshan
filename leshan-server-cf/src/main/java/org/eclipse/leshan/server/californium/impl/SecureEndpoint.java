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
package org.eclipse.leshan.server.californium.impl;

import java.net.InetSocketAddress;
import java.security.PublicKey;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.dtls.DTLSSession;

/**
 * A {@link CoAPEndpoint} for communications using DTLS security.
 */
public class SecureEndpoint extends CoAPEndpoint {

    private final DTLSConnector connector;

    public SecureEndpoint(DTLSConnector connector) {
        super(connector, NetworkConfig.getStandard());
        this.connector = connector;
    }

    /**
     * Returns the PSK identity from the DTLS session associated with the given request.
     * 
     * @param request the CoAP request
     * @return the PSK identity of the client or <code>null</code> if not found.
     */
    public String getPskIdentity(Request request) {
        return this.getSession(request).getPskIdentity();
    }

    /**
     * Returns the Raw Public Key (RPK) from the DTLS session associated with the given request.
     * 
     * @param request the CoAP request
     * @return the Raw Public Key of the client or <code>null</code> if not found.
     */
    public PublicKey getRawPublicKey(Request request) {
        return this.getSession(request).getPeerRawPublicKey();
    }

    public DTLSConnector getDTLSConnector() {
        return connector;
    }

    private DTLSSession getSession(Request request) {
        return connector.getSessionByAddress(new InetSocketAddress(request.getSource(), request.getSourcePort()));
    }
}
