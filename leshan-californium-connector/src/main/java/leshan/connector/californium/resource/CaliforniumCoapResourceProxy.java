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

import leshan.server.lwm2m.resource.LeshanResource;
import leshan.server.lwm2m.resource.RegisterResource;
import leshan.server.lwm2m.resource.proxy.CoapResourceProxy;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumCoapResourceProxy implements CoapResourceProxy{

	private ProxyCoapResource proxyCoapResource;
	private LeshanResource parentResource;

	@Override
	public void initialize(final LeshanResource parent) {
		this.proxyCoapResource = new ProxyCoapResource(parent.getResourceName());
		this.parentResource = parent;
		setResourceType(LeshanResource.RESOURCE_TYPE);
	}
	
	@Override
	public void setResourceType(final String resourceType) {
		Validate.notNull(proxyCoapResource);
		
		this.proxyCoapResource.getAttributes().addResourceType(resourceType);
	}
	
	public Resource getCoapResource() {
		return proxyCoapResource;
	}
	
	private class ProxyCoapResource extends CoapResource{

		public ProxyCoapResource(final String resourceName) {
			super(resourceName);
		}
		
		@Override
		public void handlePOST(final CoapExchange exchange) {
			parentResource.handlePOST(new CaliforniumExchangeProxy(exchange));
		}
		
		@Override
		public void handlePUT(final CoapExchange exchange) {
			parentResource.handlePUT(new CaliforniumExchangeProxy(exchange));
		}
		
		@Override
		public void handleDELETE(final CoapExchange exchange) {
			parentResource.handleDELETE(new CaliforniumExchangeProxy(exchange));
		}
		
		/*
		 * Override the default behavior so that requests to sub resources (typically /rd/{client-reg-id}) are handled by
		 * /rd resource.
		 */
		@Override
		public Resource getChild(final String name) {
			return this;
		}
	}
}
