/*
 * Copyright (c) 2013, Sierra Wireless
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
package leshan.server.lwm2m.request;

/**
 * An exception indicating a problem while accessing a resource on a LWM2M Client.
 */
public class ResourceAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final ResponseCode code;
    private final String uri;

    /**
     * Initializes all fields.
     * 
     * @param code the CoAP response code returned by the LWM2M Client or <code>null</code> if the client did not return
     *        a code, e.g. because the request timed out
     * @param uri the URI of the accessed resource
     * @param message the message returned by the server or <code>null</code> if the server did not return a message
     * @throws NullPointerException if the uri is <code>null</code>
     */
    public ResourceAccessException(ResponseCode code, String uri, String message) {
        this(code, uri, message, null);
    }

    /**
     * Initializes all fields.
     * 
     * @param code the CoAP response code returned by the LWM2M Client or <code>null</code> if the client did not return
     *        a code, e.g. because the request timed out
     * @param uri the URI of the accessed resource
     * @param message the message returned by the server or <code>null</code> if the server did not return a message
     * @param cause the root cause of the access problem
     * @throws NullPointerException if the uri is <code>null</code>
     */
    public ResourceAccessException(ResponseCode code, String uri, String message, Throwable cause) {
        super(message, cause);
        if (uri == null) {
            throw new NullPointerException("Request URI must not be null");
        }
        this.code = code;
        this.uri = uri;
    }

    /**
     * Gets the CoAP response code returned by the LWM2M Client.
     * 
     * @return the code
     */
    public ResponseCode getCode() {
        return this.code;
    }

    /**
     * Gets the URI of the accessed resource.
     * 
     * @return the URI
     */
    public String getUri() {
        return this.uri;
    }

}
