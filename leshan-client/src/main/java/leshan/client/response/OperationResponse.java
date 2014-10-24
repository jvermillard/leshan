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
package leshan.client.response;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;

public abstract class OperationResponse {

    public abstract boolean isSuccess();

    public abstract String getErrorMessage();

    public abstract ResponseCode getResponseCode();

    public abstract byte[] getPayload();

    public abstract String getLocation();

    public static OperationResponse of(final Response response) {
        return new SuccessfulOperationResponse(response);
    }

    public static OperationResponse failure(final ResponseCode responseCode, final String errorMessage) {
        return new FailedOperationResponse(responseCode, errorMessage);
    }

    private static class SuccessfulOperationResponse extends OperationResponse {
        private final Response response;

        public SuccessfulOperationResponse(final Response response) {
            this.response = response;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public ResponseCode getResponseCode() {
            return response.getCode();
        }

        @Override
        public byte[] getPayload() {
            return response.getPayload();
        }

        @Override
        public String getErrorMessage() {
            throw new UnsupportedOperationException("Successful Operations do not have Error Messages.");
        }

        @Override
        public String getLocation() {
            return response.getOptions().getLocationString();
        }

    }

    private static class FailedOperationResponse extends OperationResponse {
        private final ResponseCode responseCode;
        private final String errorMessage;

        public FailedOperationResponse(final ResponseCode responseCode, final String errorMessage) {
            this.responseCode = responseCode;
            this.errorMessage = errorMessage;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public ResponseCode getResponseCode() {
            return responseCode;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public byte[] getPayload() {
            throw new UnsupportedOperationException("Failed Operations Do Not Have Payloads... for NOW...");
        }

        @Override
        public String getLocation() {
            throw new UnsupportedOperationException("Failed Operations Do Not Have Location Paths... for NOW...");
        }

    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Response[" + isSuccess() + "|" + getResponseCode() + "]");

        return builder.toString();
    }

}
