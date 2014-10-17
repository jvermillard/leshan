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

import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;

public abstract class ResponseProxy {
	private final ResponseCode code;

	public ResponseProxy(final ResponseCode code) {
		this.code = code;
	}

	public final ResponseCode getCode(){
		return code;
	}
	
	public boolean isSuccess() {
		return true;
	}
	
	public String getErrorMessage() {
		throw new UnsupportedOperationException("No Error Messages in default ResponseProxies");
	}

	public static ResponseProxy failure(final String localizedMessage, final ResponseCode code) {
		return new FailureResponseProxy(localizedMessage, code);
	}

	private static final class FailureResponseProxy extends ResponseProxy{
		
		private final String errorMessage;

		public FailureResponseProxy(final String localizedMessage, final ResponseCode code) {
			super(code);
			this.errorMessage = localizedMessage;
		}

		@Override
		public boolean isSuccess(){
			return false;
		}
		
		@Override
		public String getErrorMessage(){
			return errorMessage;
		}
	}

}
