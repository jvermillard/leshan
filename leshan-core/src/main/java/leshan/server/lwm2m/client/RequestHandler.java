/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package leshan.server.lwm2m.client;

import java.io.UnsupportedEncodingException;

import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ContentResponse;
import leshan.server.lwm2m.message.ExecRequest;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.tlv.TlvEncoder;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Endpoint;

/**
 * A handler in charge of sending server-initiated requests to the registered clients.
 */
public class RequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);

    private final TlvEncoder tlvEncoder = new TlvEncoder();

    /** The CoAP end-point */
    private Endpoint endpoint;

    public RequestHandler(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Send a READ request to the client.
     * 
     * @param client
     * @param readRequest
     * @return the client response or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    public ClientResponse read(Client client, ReadRequest readRequest) throws InterruptedException,
            UnsupportedEncodingException {
        LOG.debug("READ request for client {}: {}", client.getEndpoint(), readRequest);

        // validate resource path
        // TODO? client.supportObject(Integer.toString(readRequest.getObjectId()));

        Request request = buildReadRequest(readRequest);
        prepareDestination(request, client);

        // send
        endpoint.sendRequest(request);
        Response coapResponse = request.waitForResponse(5000);

        if (coapResponse == null) {
            return null;
        }

        if (ResponseCode.CONTENT.equals(coapResponse.getCode())) {
            byte[] content = coapResponse.getPayload();

            // coapResponse.getOptions().getContentFormat();

            ContentFormat format = null;
            // HACK to guess the content format
            if (StringUtils.isAsciiPrintable(new String(content, "UTF-8"))) {
                format = ContentFormat.TEXT;
            } else {
                format = ContentFormat.TLV;
            }
            return new ContentResponse(content, format);
        } else {
            return new ClientResponse(coapResponse.getCode());
        }
    }

    public ClientResponse exec(Client client, ExecRequest execRequest) throws InterruptedException {
        LOG.debug("EXEC request for client {}: {}", client.getEndpoint(), execRequest);
        Request request = buildExecRequest(execRequest);

        prepareDestination(request, client);

        // send
        endpoint.sendRequest(request);
        Response coapResponse = request.waitForResponse(5000);

        if (coapResponse == null) {
            return null;
        }

        return new ClientResponse(coapResponse.getCode());
    }

    /**
     * Send a WRITE request to the client.
     * 
     * @param client
     * @param writeRequest
     * @return the client response or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    public ClientResponse write(Client client, WriteRequest writeRequest) throws InterruptedException,
            UnsupportedEncodingException {
        LOG.debug("WRITE request for client {}: {}", client.getEndpoint(), writeRequest);

        // validate resource path
        // TODO? client.supportObject(Integer.toString(writeRequest.getObjectId()));

        Request request = buildWriteRequest(writeRequest);
        prepareDestination(request, client);

        // send
        endpoint.sendRequest(request);
        Response coapResponse = request.waitForResponse(5000);

        if (coapResponse == null) {
            return null;
        }

        return new ClientResponse(coapResponse.getCode());
    }

    private Request buildReadRequest(ReadRequest readRequest) {
        Request request = Request.newGet();

        // objectId
        request.getOptions().addURIPath(Integer.toString(readRequest.getObjectId()));

        // objectInstanceId
        if (readRequest.getObjectInstanceId() == null) {
            if (readRequest.getResourceId() != null) {
                request.getOptions().addURIPath("0"); // default instanceId
            }
        } else {
            request.getOptions().addURIPath(Integer.toString(readRequest.getObjectInstanceId()));
        }

        // resourceId
        if (readRequest.getResourceId() != null) {
            request.getOptions().addURIPath(Integer.toString(readRequest.getResourceId()));
        }
        return request;
    }

    private Request buildWriteRequest(WriteRequest writeRequest) throws UnsupportedEncodingException {
        Request request = Request.newPut();

        // objectId
        request.getOptions().addURIPath(Integer.toString(writeRequest.getObjectId()));

        // objectInstanceId
        request.getOptions().addURIPath(Integer.toString(writeRequest.getObjectInstanceId()));

        // resourceId
        if (writeRequest.getResourceId() != null) {
            request.getOptions().addURIPath(Integer.toString(writeRequest.getResourceId()));
        }

        // value
        byte[] payload = null;

        switch (writeRequest.getFormat()) {
        case TEXT:
            payload = writeRequest.getStringValue().getBytes("UTF-8");
            break;
        case TLV:
            payload = tlvEncoder.encode(writeRequest.getTlvValues()).array();
            // TODO add an option for content type
            break;
        case JSON:
            throw new NotImplementedException("JSON not supported for write requests");
        default:
            throw new IllegalStateException("invalid format for write request : " + writeRequest.getFormat());
        }

        request.setPayload(payload);

        return request;
    }

    private Request buildExecRequest(ExecRequest execRequest) {
        Request request = Request.newPost();

        request.getOptions().addURIPath(Integer.toString(execRequest.getObjectId()));
        request.getOptions().addURIPath(Integer.toString(execRequest.getObjectInstanceId()));
        request.getOptions().addURIPath(Integer.toString(execRequest.getResourceId()));

        return request;
    }

    private void prepareDestination(Request request, Client client) {
        request.setDestination(client.getAddress());
        request.setDestinationPort(client.getPort());
    }

}
