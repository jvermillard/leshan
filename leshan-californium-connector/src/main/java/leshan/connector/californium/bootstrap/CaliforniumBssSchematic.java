/*
 * Copyright (c) 2014, Zebra Technologies
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
package leshan.connector.californium.bootstrap;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import leshan.connector.californium.resource.CaliforniumCoapResourceProxy;
import leshan.connector.californium.security.SecureEndpoint;
import leshan.connector.californium.server.CaliforniumLwM2mRequestSender;
import leshan.connector.californium.server.CaliforniumPskStore;
import leshan.connector.californium.server.CaliforniumServerImplementor;
import leshan.connector.californium.server.CaliforniumServerSchematic;
import leshan.server.lwm2m.bootstrap.BootstrapStore;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.impl.LwM2mBootstrapServerImpl;
import leshan.server.lwm2m.impl.bridge.bootstrap.BootstrapImplementor;
import leshan.server.lwm2m.impl.bridge.bootstrap.BootstrapSchematic;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementorSchematic;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.resource.BootstrapResource;
import leshan.server.lwm2m.security.SecurityRegistry;
import leshan.server.lwm2m.security.SecurityStore;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.scandium.DTLSConnector;

public class CaliforniumBssSchematic implements BootstrapSchematic<BootstrapServerImplementor, CaliforniumCoapResourceProxy>{

	private final Set<InetSocketAddress> enpointAddress = new HashSet<InetSocketAddress>();
	private final Set<InetSocketAddress> secureEndpointAddress = new HashSet<InetSocketAddress>();
	private SecurityStore securityStore;
	private BootstrapStore boostrapStore;
	private CoapServer coapServer;
	private CaliforniumCoapResourceProxy coapResourceProxy;

	@Override
	public CaliforniumBssSchematic addEndpoint(InetSocketAddress... localAddress) {
		Validate.notNull(localAddress, "IP address cannot be null");
		for(final InetSocketAddress address : localAddress) {
			enpointAddress.add(address);
		}
		return this;
	}

	@Override
	public CaliforniumBssSchematic addSecureEndpoint(InetSocketAddress... localSecureAddress) {
		Validate.notNull(localSecureAddress, "IP address cannot be null");
		for(final InetSocketAddress address : localSecureAddress){
			secureEndpointAddress.add(address);
		}
		return this;
	}

	@Override
	public CaliforniumBssSchematic setSecurityStore(SecurityStore securityStore) {
		this.securityStore = securityStore;
		return this;
	}

	@Override
	public CaliforniumBssSchematic setBootstrapStore(BootstrapStore bootstrapStore) {
		this.boostrapStore = bootstrapStore;
		return null;
	}
	
	@Override
	public CaliforniumBssSchematic bindResource(final CaliforniumCoapResourceProxy coapResourceProxy) {
		this.coapResourceProxy = coapResourceProxy;
		return this;
	}

	@Override
	public BootstrapServerImplementor build() {
		coapServer = new CoapServer();
		
		final Set<Endpoint> endpoints = new HashSet<Endpoint>();
		for(final InetSocketAddress address : enpointAddress) {
			final Endpoint endpoint = new CoAPEndpoint(address);
			coapServer.addEndpoint(endpoint);
			endpoints.add(endpoint);	
		}
		for(final InetSocketAddress address : secureEndpointAddress) {
			final DTLSConnector connector = new DTLSConnector(address, null);
	        connector.getConfig().setPskStore(new CaliforniumPskStore(this.securityStore));
	        final Endpoint secureEndpoint = new SecureEndpoint(connector);
	        coapServer.addEndpoint(secureEndpoint);
	        endpoints.add(secureEndpoint);
		}
		
		if(coapResourceProxy != null) {
			coapResourceProxy.initialize( new BootstrapResource(boostrapStore));
			coapServer.add(coapResourceProxy.getCoapResource());
		}
		return new BootstrapServerImplementor(coapServer, boostrapStore, securityStore);
	}

}
