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
package leshan.server.lwm2m.operation;

import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.CreateRequest;
import leshan.server.lwm2m.message.ExecRequest;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.WriteRequest;

/**
 * A set of operations that can be performed on Lightweight M2M request objects.
 */
public interface RequestHandler {

    /**
     * Reads one or more resources from a client.
     * 
     * @param readRequest the resource to read
     * @return the client response or <code>null</code> if the request timed out before the client sent a response
     * @throws ResourceAccessException if the resource could not be read
     */
    ClientResponse send(ReadRequest readRequest);

    /**
     * Executes a resource on a client.
     * 
     * @param execRequest the resource to execute and its parameters (if any)
     * @return the client response or <code>null</code> if the request timed out before the client sent a response
     * @throws ResourceAccessException if the resource could not be executed
     */
    ClientResponse send(ExecRequest execRequest);

    /**
     * Updates or replaces resources on a client.
     * 
     * @param writeRequest the resources to update or replace
     * @return the client response or <code>null</code> if the request timed out before the client sent a response
     * @throws ResourceAccessException if the resource(s) could not be written to
     */
    ClientResponse send(WriteRequest writeRequest);

    /**
     * Creates new resources on a client.
     * 
     * @param createRequest the resources to create
     * @return the client response or <code>null</code> if the request timed out before the client sent a response
     * @throws ResourceAccessException if the resource could not be created
     */
    ClientResponse send(CreateRequest createRequest);

}