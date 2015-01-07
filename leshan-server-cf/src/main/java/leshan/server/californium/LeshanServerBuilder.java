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
package leshan.server.californium;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import leshan.server.LwM2mServer;
import leshan.server.californium.impl.LeshanServer;
import leshan.server.client.ClientRegistry;
import leshan.server.impl.ClientRegistryImpl;
import leshan.server.impl.ObservationRegistryImpl;
import leshan.server.impl.SecurityRegistryImpl;
import leshan.server.observation.ObservationRegistry;
import leshan.server.security.SecurityRegistry;

/**
 * Class helping you to build and configure a Californium based Leshan Lightweight M2M server. Usage: create it, call
 * the different setters for changing the configuration and then call the {@link #build()} method for creating the
 * {@link LwM2mServer} ready to operate.
 */
public class LeshanServerBuilder {

    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    public static final int PORT = 5683;

    /** IANA assigned UDP port for CoAP with DTLS (so for LWM2M) */
    public static final int PORT_DTLS = 5684;

    private SecurityRegistry securityRegistry;
    private ObservationRegistry observationRegistry;
    private ClientRegistry clientRegistry;
    private InetSocketAddress localAddress;
    private InetSocketAddress localAddressSecure;

    public LeshanServerBuilder setLocalAddress(String hostname, int port) {
        this.localAddress = new InetSocketAddress(hostname, port);
        return this;
    }

    public LeshanServerBuilder setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    public LeshanServerBuilder setLocalAddressSecure(String hostname, int port) {
        this.localAddressSecure = new InetSocketAddress(hostname, port);
        return this;
    }

    public LeshanServerBuilder setLocalAddressSecure(InetSocketAddress localAddressSecure) {
        this.localAddressSecure = localAddressSecure;
        return this;
    }

    public LeshanServerBuilder setClientRegistry(ClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
        return this;
    }

    public LeshanServerBuilder setObservationRegistry(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
        return this;
    }

    public LeshanServerBuilder setSecurityRegistry(SecurityRegistry securityRegistry) {
        this.securityRegistry = securityRegistry;
        return this;
    }

    public LwM2mServer build() {
        if (localAddress == null)
            new InetSocketAddress((InetAddress) null, PORT);
        if (localAddressSecure == null)
            new InetSocketAddress((InetAddress) null, PORT_DTLS);
        if (clientRegistry == null)
            clientRegistry = new ClientRegistryImpl();
        if (securityRegistry == null)
            securityRegistry = new SecurityRegistryImpl();
        if (observationRegistry == null)
            observationRegistry = new ObservationRegistryImpl();
        return new LeshanServer(localAddress, localAddressSecure, clientRegistry, securityRegistry, observationRegistry);
    }
}
