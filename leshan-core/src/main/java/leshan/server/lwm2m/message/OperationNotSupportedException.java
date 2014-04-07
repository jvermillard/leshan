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


/**
 * Indicates that a particular operation is not applicable/allowed for a given
 * resource.
 */
public class OperationNotSupportedException extends ResourceAccessException {

    private static final long serialVersionUID = 1L;
    private final OperationType operation;

    public OperationNotSupportedException(OperationType op, String uri, String message) {
        this(op, uri, message, null);
    }

    public OperationNotSupportedException(OperationType op, String uri, String message, Throwable cause) {
        super(ResponseCode.METHOD_NOT_ALLOWED, uri, message, cause);
        if (op == null) {
            throw new NullPointerException("Operation must not be null");
        }
        this.operation = op;
    }

    /**
     * Gets the offending operation type.
     * 
     * @return the operation
     */
    public OperationType getOperation() {
        return this.operation;
    }

    /**
     * Returns a string representation of this exception.
     * 
     * @return the string representation
     */
    @Override
    public String toString() {

        return String.format("%s [operation: %s, responseCode: %s, request URI: %s, message: %s]", getOperation(),
                getCode(), getUri(), getMessage());
    }

}
