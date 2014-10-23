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

import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementor;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.request.LwM2mRequestSender;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.eclipse.californium.core.CoapServer;

public class CaliforniumServerImplementor implements CoapServerImplementor {

	private final CoapServer coapServer;
	private final CaliforniumLwM2mRequestSender requestSender;
	private final ClientRegistry clientRegistry;
	private final ObservationRegistry observationRegistry;
	private final SecurityRegistry securityRegistry;
	
	public CaliforniumServerImplementor(CoapServer coapServer, CaliforniumLwM2mRequestSender requestSender, 
										ClientRegistry clientRegistry, ObservationRegistry observationRegistry, 
										SecurityRegistry securityRegistry) {
		this.coapServer = coapServer;
		this.requestSender = requestSender;
		this.clientRegistry = clientRegistry;
		this.observationRegistry = observationRegistry;
		this.securityRegistry = securityRegistry;
	}

	@Override
	public LwM2mRequestSender getLWM2MRequestSender() {
		return requestSender;
	}

	@Override
	public void start() {
		coapServer.start();
		
	}

	@Override
	public void stop() {
		coapServer.stop();
		
	}

	@Override
	public void destroy() {
		coapServer.destroy();
		
	}

	@Override
	public ClientRegistry getClientRegistry() {
		return clientRegistry;
	}

	@Override
	public ObservationRegistry getObservationRegistry() {
		return observationRegistry;
	}

	@Override
	public SecurityRegistry getSecurityRegistry() {
		return securityRegistry;
	}

}
