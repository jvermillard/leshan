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
package leshan.server.lwm2m.message;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

/**
 * A response to a server request.
 */
public class ClientResponse {

    private final byte[] content;
    private final ContentFormat contentFormat;
    protected final String code;

    public ClientResponse(String code, byte[] payload, Integer contentFormatCode) {
        if (code == null) {
            throw new NullPointerException("Response code must not be null");
        }

        ContentFormat format = null;

        if (contentFormatCode != null) {
            format = ContentFormat.fromCode(contentFormatCode);
        } else if (payload != null) {
            // HACK to guess the content format from the payload
            try {
                String stringPayload = new String(payload, "UTF-8");
                if (StringUtils.isAsciiPrintable(stringPayload)) {
                    format = stringPayload.trim().startsWith("{") ? ContentFormat.JSON : ContentFormat.TEXT;
                } else {
                    format = ContentFormat.TLV;
                }
            } catch (UnsupportedEncodingException e) {
                // can safely be ignored since UTF-8 is supported by definition
                // in a JVM
            }
        }

        this.contentFormat = format;
        this.code = code;
        this.content = payload;
    }

    /**
     * Gets the CoAP repsonse code.
     * 
     * @return the code
     */
    public final String getCode() {
        return this.code;
    }

    /**
     * Gets the payload contained in the repsonse.
     * 
     * @return the payload or <code>null</code> if the client did not return any
     *         payload
     */
    public final byte[] getContent() {
        return this.content;
    }

    /**
     * Gets the content format of the response's payload.
     * 
     * @return the content format or <code>null</code> if the response did not
     *         specify a content format
     */
    public final ContentFormat getFormat() {
        return this.contentFormat;
    }

    @Override
    public String toString() {
        return String.format("ClientResponse [code=%s, content=%s, format=%s]", this.code,
                Arrays.toString(this.content), this.contentFormat);
    }

}
