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

import leshan.server.lwm2m.observation.ObserveSpec;

/**
 * A set of operations that can be performed on Lightweight M2M request objects.
 */
public interface RequestHandler {

    /**
     * Reads one or more resources from a client. Will block until a response is received from the remote client.
     * 
     * @param request the resource to read
     * @return the current value(s) of the resource(s)
     * @throws ResourceAccessException if the resource could not be read
     */
    ClientResponse send(ReadRequest request);

    /**
     * Reads one or more resources from a client.
     * 
     * @param request the resource to read
     * @param callback the callback to be notified of the response
     */
    void send(ReadRequest request, ResponseCallback callback);

    /**
     * Starts observation of one or more resources implemented by a client. Will block until a response is received from
     * the remote client.
     * 
     * @param request the resource(s) to observe
     * @return the current value(s) of the resource(s) along with an <em>observation ID</em> which can be used to cancel
     *         the observation later on
     * @throws ResourceAccessException if the resource could not be read
     */
    ClientResponse send(ObserveRequest request);

    /**
     * Starts observation of one or more resources implemented by a client.
     * 
     * @param request the resource(s) to observe
     * @param callback the callback to be notified of the response
     */
    void send(ObserveRequest request, ResponseCallback callback);

    /**
     * Sets conditions a client is supposed to consider when sending notifications for resources observed by the LWM2M
     * Server. Will block until a response is received from the remote client.
     * 
     * This operation can also be used to cancel observation of resources by means of setting the <em>cancel</em>
     * attribute (see {@link ObserveSpec}).
     * 
     * @param request the parameters
     * @return an empty response if the update succeeded
     * @throws ResourceAccessException if the attributes could not be set/updated
     */
    ClientResponse send(WriteAttributesRequest request);

    /**
     * Sets conditions a client is supposed to consider when sending notifications for resources observed by the LWM2M
     * Server.
     * 
     * This operation can also be used to cancel observation of resources by means of setting the <em>cancel</em>
     * attribute (see {@link ObserveSpec}).
     * 
     * @param request the parameters
     * @param callback the callback to be notified of the response
     */
    void send(WriteAttributesRequest request, ResponseCallback callback);

    /**
     * Executes a resource on a client. Will block until a response is received from the remote client.
     * 
     * @param request the resource to execute and its parameters (if any)
     * @return the client response or <code>null</code> if the request timed out before the client sent a response
     * @throws ResourceAccessException if the resource could not be executed
     */
    ClientResponse send(ExecRequest request);

    /**
     * Executes a resource on a client.
     * 
     * @param request the resource to execute and its parameters (if any)
     * @param callback the callback to be notified of the response
     */
    void send(ExecRequest request, ResponseCallback callback);

    /**
     * Updates or replaces resources on a client. Will block until a response is received from the remote client.
     * 
     * @param request the resources to update or replace
     * @return the client response or <code>null</code> if the request timed out before the client sent a response
     * @throws ResourceAccessException if the resource(s) could not be written to
     */
    ClientResponse send(WriteRequest request);

    /**
     * Updates or replaces resources on a client.
     * 
     * @param request the resources to update or replace
     * @param callback the callback to be notified of the response
     */
    void send(WriteRequest request, ResponseCallback callback);

    /**
     * Creates new resources on a client. Will block until a response is received from the remote client.
     * 
     * @param request the resources to create
     * @return the client response or <code>null</code> if the request timed out before the client sent a response
     * @throws ResourceAccessException if the resource could not be created
     */
    ClientResponse send(CreateRequest request);

    /**
     * Creates new resources on a client.
     * 
     * @param request the resources to create
     * @param callback the callback to be notified of the response
     */
    void send(CreateRequest request, ResponseCallback callback);

    /**
     * Discovers resources and their attributes implemented by a client. Will block until a response is received from
     * the remote client.
     * 
     * @param request the resources to discover
     * @return the implemented resources and their attributes represented as CoRE links
     * @throws ResourceAccessException if the resources could not be discovered
     */
    ClientResponse send(DiscoverRequest request);

    /**
     * Discovers resources and their attributes implemented by a client.
     * 
     * @param request the resources to discover
     * @param callback the callback to be notified of the response
     */
    void send(DiscoverRequest request, ResponseCallback callback);

    /**
     * Deletes an object instance on a client. Will block until a response is received from the remote client.
     * 
     * @param request the object instance to delete
     * @return the response from the client if the object has been deleted
     * @throws ResourceAccessException if the resource could not be deleted
     */
    ClientResponse send(DeleteRequest request);

    /**
     * Deletes an object instance on a client.
     * 
     * @param request the object instance to delete
     * @param callback the callback to be notified of the response
     */
    void send(DeleteRequest request, ResponseCallback callback);
}