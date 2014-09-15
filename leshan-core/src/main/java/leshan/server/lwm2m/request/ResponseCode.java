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

import org.apache.commons.lang.Validate;

import ch.ethz.inf.vs.californium.coap.CoAP;

/**
 * Response codes defined for LWM2M enabler
 */
public enum ResponseCode {

    /** Resource correctly created */
    CREATED,
    /** Resource correctly deleted */
    DELETED,
    /** Resource correctly changed */
    CHANGED,
    /** Content correctly delivered */
    CONTENT,
    /** Operation not authorized */
    UNAUTHORIZED,
    /** Cannot fulfill the request, it's incorrect */
    BAD_REQUEST,
    /** This method (GET/PUT/POST/DELETE) is not allowed on this resource */
    METHOD_NOT_ALLOWED,
    /** The End-point Client Name results in a duplicate entry on the LWM2M Server */
    CONFLICT,
    /** Resource not found */
    NOT_FOUND;

    public static ResponseCode fromCoapCode(int code) {
        Validate.notNull(code);

        if (code == CoAP.ResponseCode.CREATED.value) {
            return CREATED;
        } else if (code == CoAP.ResponseCode.DELETED.value) {
            return DELETED;
        } else if (code == CoAP.ResponseCode.CHANGED.value) {
            return CHANGED;
        } else if (code == CoAP.ResponseCode.CONTENT.value) {
            return CONTENT;
        } else if (code == CoAP.ResponseCode.BAD_REQUEST.value) {
            return BAD_REQUEST;
        } else if (code == CoAP.ResponseCode.UNAUTHORIZED.value) {
            return UNAUTHORIZED;
        } else if (code == CoAP.ResponseCode.NOT_FOUND.value) {
            return NOT_FOUND;
        } else if (code == CoAP.ResponseCode.METHOD_NOT_ALLOWED.value) {
            return METHOD_NOT_ALLOWED;
        } else if (code == 137) {
            return CONFLICT;
        } else {
            throw new IllegalArgumentException("Invalid CoAP code for LWM2M response: " + code);
        }
    }
}
