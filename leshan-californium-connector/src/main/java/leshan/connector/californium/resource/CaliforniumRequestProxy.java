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
package leshan.connector.californium.resource;

import java.util.List;

import leshan.connector.californium.server.CaliforniumResponseCode;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;
import leshan.server.lwm2m.resource.proxy.RequestProxy;
import leshan.server.lwm2m.resource.proxy.ResponseProxy;

import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Endpoint;

public class CaliforniumRequestProxy extends RequestProxy {
	private static final CaliforniumResponseCode CALIFORNIUM_RESPONSE_CODE = new CaliforniumResponseCode();

	private final Request request;
	private Endpoint endpoint;

	public CaliforniumRequestProxy(final Request request) {
		this.request = request;
	}

	public CaliforniumRequestProxy(final Request request, final Endpoint e) {
		this(request);
		this.endpoint = e;
	}

	@Override
	public boolean isConfirmable() {
		return Type.CON.equals(request.getType());
	}

	@Override
	public List<String> getURIQueries() {
		return request.getOptions().getURIQueries();
	}

	@Override
	public boolean hasPayload() {
		return request.getPayload() != null;
	}

	@Override
	public byte[] getPayload() {
		return request.getPayload();
	}

	@Override
	public ResponseProxy sendAndWaitForResponse(final int timeoutMilli) {
        Response response;
		try {
			response = request.send(endpoint).waitForResponse(timeoutMilli);
			if(response == null){
				return ResponseProxy.failure("Timeout", ResponseCode.NOT_FOUND);
			}
			else{
				return new CaliforniumResponseProxy(response, CALIFORNIUM_RESPONSE_CODE.fromCoapCode(response.getCode().ordinal()));
			}
		} catch (final InterruptedException e) {
			return ResponseProxy.failure(e.getLocalizedMessage(), ResponseCode.BAD_REQUEST);
		}
        
	}

}
