/*
 * Copyright (c) 2014, Sierra Wireless
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
package leshan.server.californium.impl;

import java.net.InetSocketAddress;
import java.util.Arrays;

import leshan.server.client.Client;
import leshan.server.client.ClientRegistry;
import leshan.server.security.SecurityInfo;
import leshan.server.security.SecurityStore;

import org.eclipse.californium.scandium.dtls.pskstore.PskStore;

public class LwM2mPskStore implements PskStore {

    private SecurityStore securityStore;
    private ClientRegistry clientRegistry;

    public LwM2mPskStore(SecurityStore securityStore) {
        this(securityStore, null);
    }

    public LwM2mPskStore(SecurityStore securityStore, ClientRegistry clientRegistry) {
        this.securityStore = securityStore;
        this.clientRegistry = clientRegistry;
    }

    @Override
    public byte[] getKey(String identity) {
        SecurityInfo info = securityStore.getByIdentity(identity);
        if (info == null || info.getPreSharedKey() == null) {
            return null;
        } else {
            // defensive copy
            return Arrays.copyOf(info.getPreSharedKey(), info.getPreSharedKey().length);
        }
    }

    @Override
    public String getIdentity(InetSocketAddress inetAddress) {
        if (clientRegistry == null)
            return null;

        for (Client c : clientRegistry.allClients()) {
            if (inetAddress.getPort() == c.getPort() && inetAddress.getAddress().equals(c.getAddress())) {
                SecurityInfo securityInfo = securityStore.getByEndpoint(c.getEndpoint());
                if (securityInfo != null) {
                    return securityInfo.getIdentity();
                }
                return null;
            }
        }
        return null;
    }
}
