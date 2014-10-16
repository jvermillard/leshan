/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 *
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
package leshan.client.lwm2m.response;

import java.util.Objects;

import leshan.server.lwm2m.request.ResponseCode;


public class CreateResponse extends BaseLwM2mResponse {

	private final String location;

	private CreateResponse(final ResponseCode code, final String location) {
		super(code, new byte[0]);
		this.location = location;
	}

	private CreateResponse(final ResponseCode code) {
		this(code, null);
	}

	public static CreateResponse success(final int instanceId) {
		return new CreateResponse(ResponseCode.CREATED, Integer.toString(instanceId));
	}

	public static CreateResponse methodNotAllowed() {
		return new CreateResponse(ResponseCode.METHOD_NOT_ALLOWED);
	}

	public static CreateResponse invalidResource() {
		return new CreateResponse(ResponseCode.BAD_REQUEST);
	}

	public String getLocation() {
		return location;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof CreateResponse) || !super.equals(o)) {
			return false;
		}
		final CreateResponse other = (CreateResponse)o;
		return Objects.equals(location, other.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), location);
	}

}
