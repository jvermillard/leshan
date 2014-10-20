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
package leshan.server.lwm2m.resource;

import leshan.server.lwm2m.resource.proxy.CoapResourceProxy;
import leshan.server.lwm2m.resource.proxy.ExchangeProxy;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LeshanResource {
	private static final Logger LOG = LoggerFactory.getLogger(LeshanResource.class);
	
	public static final String RESOURCE_TYPE = "core.rd";

	public static final String QUERY_PARAM_ENDPOINT = "ep=";

	public static final String QUERY_PARAM_BINDING_MODE = "b=";

	public static final String QUERY_PARAM_LWM2M_VERSION = "lwm2m=";

	public static final String QUERY_PARAM_SMS = "sms=";

	public static final String QUERY_PARAM_LIFETIME = "lt=";
	
	public final String resourceName;
	

	private CoapResourceProxy coapResourceProxy;

	public LeshanResource(String resourceName) {
		this.resourceName = resourceName;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public void setCoapResourceFactory(final CoapResourceProxy factory){
		this.coapResourceProxy = factory;
	}
	
	public CoapResourceProxy getCoapResourceProxy() {
		Validate.notNull(coapResourceProxy);
		
		return coapResourceProxy;
	}

	public void handlePOST(final ExchangeProxy exchangeProxy){
		LOG.debug("Doing nothing by default.");
	}

	public void handlePUT(final ExchangeProxy exchangeProxy){
		LOG.debug("Doing nothing by default.");
	}

	public void handleDELETE(final ExchangeProxy exchangeProxy){
		LOG.debug("Doing nothing by default.");
	}
}
