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
package leshan.connector.californium.server;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.scandium.DTLSConnector;

import leshan.connector.californium.resource.CaliforniumCoapResourceProxy;
import leshan.connector.californium.security.SecureEndpoint;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementorSchematic;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.resource.LeshanResource;
import leshan.server.lwm2m.resource.RegisterResource;
import leshan.server.lwm2m.security.SecurityRegistry;

public class CaliforniumServerSchematic implements CoapServerImplementorSchematic<CaliforniumServerImplementor, CaliforniumCoapResourceProxy> {
	
	private final Set<InetSocketAddress> enpointAddress = new HashSet<InetSocketAddress>();
	private final Set<InetSocketAddress> secureEndpointAddress = new HashSet<InetSocketAddress>();
	private ClientRegistry clientRegistry;
	private SecurityRegistry securityRegistry;
	private ObservationRegistry observationRegistry;
	private CaliforniumCoapResourceProxy coapResourceProxy;
	private CoapServer coapServer;
	private CaliforniumLwM2mRequestSender requestSender;
	
	@Override
	public CaliforniumServerSchematic addEndpoint(InetSocketAddress... localAddress) {
		Validate.notNull(localAddress, "IP address cannot be null");
		for(InetSocketAddress address : localAddress) {
			enpointAddress.add(address);
		}
		return this;
	}

	@Override
	public CaliforniumServerSchematic addSecureEndpoint(InetSocketAddress... localSecureAddress) {
		Validate.notNull(localSecureAddress, "IP address cannot be null");
		for(InetSocketAddress address : localSecureAddress){
			secureEndpointAddress.add(address);
		}
		return this;
	}

	@Override
	public CaliforniumServerSchematic bindResource(CaliforniumCoapResourceProxy coapResourceProxy) {
		this.coapResourceProxy = coapResourceProxy;
		return this;
	}

	@Override
	public CaliforniumServerSchematic setClientRegistry(ClientRegistry clientRegistry) {
		this.clientRegistry = clientRegistry;
		return this;
	}

	@Override
	public CaliforniumServerSchematic setSecurityRegistry(SecurityRegistry securityRegistry) {
		this.securityRegistry = securityRegistry;
		return this;
	}

	@Override
	public CaliforniumServerSchematic setObservationRegistry(ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
		return this;
	}

	@Override
	public CaliforniumServerImplementor buildCoapServerImplementor() {
		coapServer = new CoapServer();
		
		final Set<Endpoint> endpoints = new HashSet<Endpoint>();
		for(InetSocketAddress address : enpointAddress) {
			Endpoint endpoint = new CoAPEndpoint(address);
			coapServer.addEndpoint(endpoint);
			endpoints.add(endpoint);	
		}
		for(InetSocketAddress address : secureEndpointAddress) {
			final DTLSConnector connector = new DTLSConnector(address, null);
	        connector.getConfig().setPskStore(new CaliforniumPskStore(this.securityRegistry, this.clientRegistry));
	
	        final Endpoint secureEndpoint = new SecureEndpoint(connector);
	        coapServer.addEndpoint(secureEndpoint);
	        endpoints.add(secureEndpoint);
		}
		
		requestSender = new CaliforniumLwM2mRequestSender(endpoints, observationRegistry);
		coapResourceProxy.initialize(new RegisterResource(clientRegistry, securityRegistry));
//		final RegisterResource rdResource = new RegisterResource(clientRegistry, securityRegistry, coapResourceProxy);
		coapServer.add(coapResourceProxy.getCoapResource());
		
		return new CaliforniumServerImplementor(coapServer, requestSender, clientRegistry, observationRegistry, securityRegistry);
	}
}
