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
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leshan.server.lwm2m.RequestHandler;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ExecRequest;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.servlet.json.ClientSerializer;
import leshan.server.servlet.json.ResponseSerializer;
import leshan.server.servlet.json.TlvSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
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

    private final RequestHandler requestHandler;

    private final ClientRegistry clientRegistry;

    private final Gson gson;

    public ApiServlet(RequestHandler requestHandler, ClientRegistry clientRegistry) {
        this.requestHandler = requestHandler;
        this.clientRegistry = clientRegistry;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Tlv.class, new TlvSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(Client.class, new ClientSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(ClientResponse.class, new ResponseSerializer());
        gson = gsonBuilder.create();
    }

    private boolean checkPath(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] path = StringUtils.split(req.getPathInfo(), '/');

        if (ArrayUtils.isEmpty(path)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        if (!("clients".equals(path[0]))) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "not found: '" + req.getPathInfo() + "'");
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkPath(req, resp)) {
            return;
        }

        String[] path = StringUtils.split(req.getPathInfo(), '/');

        if (path.length == 1) { // all registered clients
            Collection<Client> clients = clientRegistry.allClients();

            String json = gson.toJson(clients.toArray(new Client[] {}));
            resp.setContentType("application/json");
            resp.getOutputStream().write(json.getBytes("UTF-8"));
            resp.setStatus(HttpServletResponse.SC_OK);

        } else if (path.length == 2) { // get client
            String clientEndpoint = path[1];
            Client client = clientRegistry.get(clientEndpoint);
            if (client != null) {
                resp.setContentType("application/json");
                resp.getOutputStream().write(gson.toJson(client).getBytes("UTF-8"));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "unknown client " + clientEndpoint);
            }
        } else {
            try {
                RequestInfo requestInfo = new RequestInfo(path);

                Client client = clientRegistry.get(requestInfo.endpoint);
                if (client == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no registered client with id '"
                            + requestInfo.endpoint + "'");
                } else {
                    ClientResponse cResponse = this.readRequest(client, requestInfo, resp);
                    processDeviceResponse(resp, cResponse);
                }
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

            } catch (NotImplementedException e) {
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, e.getMessage());
            } catch (Exception e) {
                LOG.error("unexpected error", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkPath(req, resp)) {
            return;
        }

        String[] path = StringUtils.split(req.getPathInfo(), '/');

        try {
            RequestInfo requestInfo = new RequestInfo(path);

            Client client = clientRegistry.get(requestInfo.endpoint);
            if (client == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no registered client with id '"
                        + requestInfo.endpoint + "'");
            } else {
                ClientResponse cResponse = this.writeRequest(client, requestInfo, req, resp);
                processDeviceResponse(resp, cResponse);
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

        } catch (NotImplementedException e) {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, e.getMessage());
        } catch (Exception e) {
            LOG.error("unexpected error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkPath(req, resp)) {
            return;
        }

        String[] path = StringUtils.split(req.getPathInfo(), '/');

        try {
            RequestInfo requestInfo = new RequestInfo(path);

            Client client = clientRegistry.get(requestInfo.endpoint);
            if (client == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no registered client with id '"
                        + requestInfo.endpoint + "'");

            } else {
                ClientResponse cResponse = this.execRequest(client, requestInfo, resp);
                processDeviceResponse(resp, cResponse);
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

        } catch (NotImplementedException e) {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, e.getMessage());
        } catch (Exception e) {
            LOG.error("unexpected error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void processDeviceResponse(HttpServletResponse resp, ClientResponse cResponse) throws IOException {
        String response = null;
        if (cResponse == null) {
            response = "Request timeout";
        } else {
            response = gson.toJson(cResponse);
        }
        resp.setContentType("application/json");
        resp.getOutputStream().write(response.getBytes());

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private ClientResponse readRequest(Client client, RequestInfo requestInfo, HttpServletResponse resp)
            throws InterruptedException, ExecutionException, IOException {

        return requestHandler.read(client, new ReadRequest(requestInfo.objectId, requestInfo.objectInstanceId,
                requestInfo.resourceId));
    }

    private ClientResponse execRequest(Client client, RequestInfo requestInfo, HttpServletResponse resp)
            throws InterruptedException, ExecutionException, IOException {

        return requestHandler.exec(client, new ExecRequest(requestInfo.objectId, requestInfo.objectInstanceId,
                requestInfo.resourceId));
    }

    private ClientResponse writeRequest(Client client, RequestInfo requestInfo, HttpServletRequest req,
            HttpServletResponse resp) throws InterruptedException, ExecutionException, IOException {

        if ("text/plain".equals(req.getContentType())) {
            String content = IOUtils.toString(req.getInputStream(), "UTF-8");
            return requestHandler.write(client, new WriteRequest(requestInfo.objectId, requestInfo.objectInstanceId,
                    requestInfo.resourceId, ContentFormat.TEXT, content, null));
        } else {
            throw new NotImplementedException("content type " + req.getContentType()
                    + " not supported for write requests");
        }
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