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
package leshan.server.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leshan.server.lwm2m.LwM2mRequestFilter;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.client.ClientResponse;
import leshan.server.lwm2m.message.client.ContentResponse;
import leshan.server.lwm2m.message.server.ReadRequest;
import leshan.server.lwm2m.message.server.WriteRequest;
import leshan.server.lwm2m.session.LwSession;
import leshan.server.lwm2m.session.SessionRegistry;
import leshan.server.servlet.json.Client;
import leshan.server.servlet.json.Response;
import leshan.server.servlet.json.TlvSerializer;
import leshan.server.tlv.Tlv;
import leshan.server.tlv.TlvDecoder;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.mina.api.IoFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Service HTTP REST API calls.
 */
public class ApiServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ApiServlet.class);

    private static final long serialVersionUID = 1L;

    private final SessionRegistry registry;

    private final LwM2mRequestFilter requestFilter;

    private final Gson gson;

    private final TlvDecoder tlvDecoder = new TlvDecoder();

    public ApiServlet(SessionRegistry registry, LwM2mRequestFilter requestFilter) {
        this.registry = registry;
        this.requestFilter = requestFilter;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Tlv.class, new TlvSerializer());

        gson = gsonBuilder.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleRequest(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String[] path = StringUtils.split(req.getPathInfo(), '/');

        if (ArrayUtils.isEmpty(path)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            if (!("clients".equals(path[0]))) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "not found: '" + req.getPathInfo() + "'");
                return;
            }

            if (path.length == 1 && "GET".equals(req.getMethod())) {
                // list registered clients
                List<Client> clients = new ArrayList<>();
                for (LwSession session : registry.allSessions()) {
                    clients.add(new Client(session.getEndpoint(), session.getRegistrationId(), session.getIoSession()
                            .getRemoteAddress().toString(), session.getObjects(), session.getSmsNumber(), session
                            .getLwM2mVersion(), session.getLifeTimeInSec()));
                }

                String json = gson.toJson(clients.toArray(new Client[] {}));
                resp.setContentType("application/json");
                resp.getOutputStream().write(json.getBytes());

                resp.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            RequestInfo requestInfo;
            try {
                requestInfo = new RequestInfo(path);
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }

            LwSession session = registry.getSession(requestInfo.endpoint);
            if (session == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no registered client with id '"
                        + requestInfo.endpoint + "'");
                return;
            }

            if ("GET".equals(req.getMethod())) {
                // read
                this.readRequest(session, requestInfo, resp);
            } else if ("PUT".equals(req.getMethod())) {
                // write
                this.writeRequest(session, requestInfo, req, resp);
            }
            return;

        } catch (NotImplementedException e) {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, e.getMessage());
        } catch (Exception e) {
            LOG.error("unexpected error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private void readRequest(LwSession session, RequestInfo requestInfo, HttpServletResponse resp)
            throws InterruptedException, ExecutionException, IOException {

        ReadRequest request = new ReadRequest(requestInfo.objectId, requestInfo.objectInstanceId,
                requestInfo.resourceId);
        IoFuture<ClientResponse> future = requestFilter.request(session.getIoSession(), request, 5000);
        // wait for client response
        ClientResponse lwResponse = future.get();

        // build JSON read response
        Object value = null;
        if (lwResponse instanceof ContentResponse) {
            ContentResponse cResponse = (ContentResponse) lwResponse;
            switch (cResponse.getFormat()) {
            case TLV:
                value = tlvDecoder.decode(ByteBuffer.wrap(cResponse.getContent()), null);
                break;
            case TEXT:
            case JSON:
            case LINK:
                value = new String(cResponse.getContent(), "UTF-8");
                break;
            case OPAQUE:
                value = Hex.encodeHexString(cResponse.getContent());
                break;
            }
        }

        Response response = new Response(lwResponse.getCode().toString(), value);

        String json = gson.toJson(response);
        resp.setContentType("application/json");
        resp.getOutputStream().write(json.getBytes());

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void writeRequest(LwSession session, RequestInfo requestInfo, HttpServletRequest req,
            HttpServletResponse resp) throws InterruptedException, ExecutionException, IOException {

        WriteRequest request = null;
        if ("text/plain".equals(req.getContentType())) {
            String content = IOUtils.toString(req.getInputStream(), "UTF-8");
            request = new WriteRequest(requestInfo.objectId, requestInfo.objectInstanceId, requestInfo.resourceId,
                    ContentFormat.TEXT, content, null);
        } else {
            throw new NotImplementedException("content type " + req.getContentType()
                    + " not supported for write requests");
        }

        IoFuture<ClientResponse> future = requestFilter.request(session.getIoSession(), request, 5000);
        // wait for client response
        ClientResponse lwResponse = future.get();

        // build JSON response
        Response response = new Response(lwResponse.getCode().toString(), null);

        String json = gson.toJson(response);
        resp.setContentType("application/json");
        resp.getOutputStream().write(json.getBytes());

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    class RequestInfo {

        String endpoint;
        Integer objectId;
        Integer objectInstanceId;
        Integer resourceId;
        Integer resourceInstanceId;

        /**
         * Build LW request info from URI path
         */
        RequestInfo(String[] path) {

            if (path.length < 3 || path.length > 6) {
                throw new IllegalArgumentException("invalid path");
            }

            this.endpoint = path[1];

            try {
                this.objectId = Integer.valueOf(path[2]);

                if (path.length > 3) {
                    this.objectInstanceId = Integer.valueOf(path[3]);
                }
                if (path.length > 4) {
                    this.resourceId = Integer.valueOf(path[4]);
                }
                if (path.length > 5) {
                    this.resourceInstanceId = Integer.valueOf(path[5]);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid path", e);
            }
        }
    }
}