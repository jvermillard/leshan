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
 */package leshan.connector.californium.resource;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import leshan.connector.californium.security.SecureEndpoint;
import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.resource.proxy.ExchangeProxy;
import leshan.server.lwm2m.resource.proxy.RequestProxy;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CaliforniumExchangeProxy extends ExchangeProxy {

	private final CoapExchange exchange;
	private final RequestProxy request;

	public CaliforniumExchangeProxy(final CoapExchange exchange) {
		this.exchange = exchange;
		this.request = new CaliforniumRequestProxy(exchange.advanced().getRequest());
	}

	@Override
	public RequestProxy getRequest() {
		return request;
	}

	@Override
	public void respond(
			final leshan.server.lwm2m.request.CoapResponseCode.ResponseCode code, final String... errorMessage) {
		ResponseCode caCode = null;

		switch(code){
		case BAD_REQUEST:
			caCode = ResponseCode.BAD_REQUEST;
			break;
		case CREATED:
			caCode = ResponseCode.CREATED;
			break;
		case NOT_FOUND:
			caCode = ResponseCode.NOT_FOUND;
			break;
		case CHANGED:
			caCode = ResponseCode.CHANGED;
			break;
		case DELETED:
			caCode = ResponseCode.DELETED;
			break;
		case CONTENT:
			caCode = ResponseCode.CONTENT;
			break;
		default:
			caCode = ResponseCode.INTERNAL_SERVER_ERROR;

		}

		if(errorMessage == null || errorMessage.length == 0){
			exchange.respond(caCode);
		}
		else{
			exchange.respond(caCode, errorMessage[0]);
		}

	}

	@Override
	public InetSocketAddress getEndpointAddress() {
		return exchange.advanced().getEndpoint().getAddress();
	}

	@Override
	public boolean isUsingSecureEndpoint() {
		return exchange.advanced().getEndpoint() instanceof SecureEndpoint;
	}

	@Override
	public String getPskIdentity() {
		if (exchange.advanced().getEndpoint() instanceof SecureEndpoint) {
			return ((SecureEndpoint) exchange.advanced().getEndpoint()).getPskIdentity(exchange.advanced().getRequest());
		}

		return null;
	}

	@Override
	public void killTlsSession() {
		final Request californiumRequest = exchange.advanced().getRequest();

		((SecureEndpoint) exchange.advanced().getEndpoint()).getDTLSConnector().close(
				new InetSocketAddress(californiumRequest.getSource(), californiumRequest.getSourcePort()));
	}

	@Override
	public Client createNewClient(final String registrationId, final String endpoint, final String lwVersion,
			final Long lifetime, final String smsNumber, final BindingMode binding,
			final LinkObject[] objectLinks, final InetSocketAddress registrationEndpoint) {
		final Request californiumRequest = exchange.advanced().getRequest();

		final Client client = new Client(registrationId, endpoint, californiumRequest.getSource(), californiumRequest.getSourcePort(),
				lwVersion, lifetime, smsNumber, binding, objectLinks, registrationEndpoint);

		return client;
	}

	@Override
	public void setLocationPath(final String locationPath) {
		exchange.setLocationPath(locationPath);
	}

	@Override
	public List<String> getURIPaths() {
		return exchange.getRequestOptions().getURIPaths();
	}

	@Override
	public ClientUpdate createNewClientUpdate(final String registrationId, final Long lifetime,
			final String smsNumber, final BindingMode binding, final LinkObject[] objectLinks) {
		final Request californiumRequest = exchange.advanced().getRequest();
		
		final ClientUpdate client = new ClientUpdate(registrationId, californiumRequest.getSource(), californiumRequest.getSourcePort(), lifetime,
		smsNumber, binding, objectLinks);
		
		return client;
		
	}

	@Override
	public List<String> getUQRIQueries() {
        return exchange.advanced().getRequest().getOptions().getURIQueries();
	}

	@Override
	public RequestProxy createDeleteAllRequest() {
        final Endpoint e = exchange.advanced().getEndpoint();
        final Request deleteAll = Request.newDelete();
        deleteAll.getOptions().addURIPath("/");
        deleteAll.setConfirmable(true);
        deleteAll.setDestination(exchange.getSourceAddress());
        deleteAll.setDestinationPort(exchange.getSourcePort());
        
		return new CaliforniumRequestProxy(deleteAll, e);
	}

	@Override
	public RequestProxy createPostSecurityRequest(final ByteBuffer encoded) {
		final Endpoint e = exchange.advanced().getEndpoint();
		final Request postSecurity = Request.newPost();
		postSecurity.getOptions().addURIPath("/0");
		postSecurity.setConfirmable(true);
		postSecurity.setDestination(exchange.getSourceAddress());
		postSecurity.setDestinationPort(exchange.getSourcePort());
		postSecurity.setPayload(encoded.array());
		
		return new CaliforniumRequestProxy(postSecurity, e);
	}

	@Override
	public RequestProxy createPostServerRequest(final ByteBuffer encoded) {
		final Endpoint e = exchange.advanced().getEndpoint();
		final Request postServer = Request.newPost();
		postServer.getOptions().addURIPath("/1");
		postServer.setConfirmable(true);
		postServer.setDestination(exchange.getSourceAddress());
		postServer.setDestinationPort(exchange.getSourcePort());
		postServer.setPayload(encoded.array());
		
		return new CaliforniumRequestProxy(postServer, e);
	}

}
