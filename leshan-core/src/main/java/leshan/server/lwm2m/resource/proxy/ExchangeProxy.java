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
package leshan.server.lwm2m.resource.proxy;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;


public abstract class ExchangeProxy {

	public abstract RequestProxy getRequest();

	public abstract InetSocketAddress getEndpointAddress();

	public abstract boolean isUsingSecureEndpoint();

	public abstract String getPskIdentity();

	public abstract void killTlsSession();

	public abstract Client createNewClient(String registrationId, String endpoint,
			String lwVersion, Long lifetime, String smsNumber, BindingMode binding,
			LinkObject[] objectLinks, InetSocketAddress registrationEndpoint);

	public abstract void setLocationPath(String locationPath);

	public abstract List<String> getURIPaths();

	public abstract void respond(ResponseCode code, final String... errorMessage);

	public abstract ClientUpdate createNewClientUpdate(String registrationId, Long lifetime,
			String smsNumber, BindingMode binding, LinkObject[] objectLinks);

	public abstract List<String> getUQRIQueries();

	public abstract RequestProxy createDeleteAllRequest();

	public abstract RequestProxy createPostSecurityRequest(ByteBuffer encoded);

	public abstract RequestProxy createPostServerRequest(ByteBuffer encoded);

}
