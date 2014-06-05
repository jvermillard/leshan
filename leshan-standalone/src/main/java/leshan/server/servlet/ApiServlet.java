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
package leshan.server.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ExecRequest;
import leshan.server.lwm2m.message.ObserveRequest;
import leshan.server.lwm2m.message.ObserveResponse;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.RequestHandler;
import leshan.server.lwm2m.message.ResourceAccessException;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.observation.ResourceObserver;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.servlet.json.ClientSerializer;
import leshan.server.servlet.json.ResponseSerializer;
import leshan.server.servlet.json.TlvSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpFields;
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
    private final ResourceObserver resourceObserver;
    private final ClientRegistry clientRegistry;

    private final Gson gson;

    public ApiServlet(RequestHandler requestHandler, ClientRegistry clientRegistry, ResourceObserver observer) {
        this.requestHandler = requestHandler;
        this.clientRegistry = clientRegistry;
        this.resourceObserver = observer;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Tlv.class, new TlvSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(Client.class, new ClientSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(ClientResponse.class, new ResponseSerializer());
        this.gson = gsonBuilder.create();
    }

    private boolean checkPath(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] path = StringUtils.split(req.getPathInfo(), '/');
        if (ArrayUtils.isEmpty(path) || (!"clients".equals(path[0]))) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().format("%s%s api does not exist", req.getServletPath(), req.getPathInfo()).flush();
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

        // /clients : all registered clients
        if (path.length == 1) {
            Collection<Client> clients = this.clientRegistry.allClients();

            String json = this.gson.toJson(clients.toArray(new Client[] {}));
            resp.setContentType("application/json");
            resp.getOutputStream().write(json.getBytes("UTF-8"));
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // /clients/endPoint : get client
        if (path.length == 2) {
            String clientEndpoint = path[1];
            Client client = this.clientRegistry.get(clientEndpoint);
            if (client != null) {
                resp.setContentType("application/json");
                resp.getOutputStream().write(this.gson.toJson(client).getBytes("UTF-8"));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("no registered client with id '%s'", clientEndpoint).flush();
            }
            return;
        }

        // /clients/endPoint/LWRequest : do LightWeight M2M read request on a given client.
        boolean isObserveRequest = req.getParameter("obs") != null;
        try {
            RequestInfo requestInfo = new RequestInfo(path);
            Client client = this.clientRegistry.get(requestInfo.endpoint);
            if (client != null) {
                ClientResponse cResponse = null;
                if (isObserveRequest) {
                    cResponse = this.observeRequest(client, requestInfo, resp);
                } else {
                    cResponse = this.readRequest(client, requestInfo, resp);
                }
                processDeviceResponse(resp, cResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("no registered client with id '%s'", requestInfo.endpoint).flush();
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append(e.getMessage()).flush();
        } catch (ResourceAccessException e) {
            LOG.debug(String.format("Error accessing resource %s%s.", req.getServletPath(), req.getPathInfo()), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append(e.getMessage()).flush();
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

        // /clients/endPoint/LWRequest : do LightWeight M2M write request on a given client.
        String[] path = StringUtils.split(req.getPathInfo(), '/');
        try {
            RequestInfo requestInfo = new RequestInfo(path);
            Client client = this.clientRegistry.get(requestInfo.endpoint);
            if (client != null) {
                ClientResponse cResponse = this.writeRequest(client, requestInfo, req, resp);
                processDeviceResponse(resp, cResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("no registered client with id '%s'", requestInfo.endpoint).flush();
            }
        } catch (IllegalArgumentException e) {
            // content encoding other than text/plain is not supported (yet)
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append(e.getMessage()).flush();
        } catch (ResourceAccessException e) {
            LOG.debug(String.format("Error accessing resource %s%s.", req.getServletPath(), req.getPathInfo()), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append(e.getMessage()).flush();
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

        // /clients/endPoint/LWRequest : do LightWeight M2M execute request on a given client.
        String[] path = StringUtils.split(req.getPathInfo(), '/');
        try {
            RequestInfo requestInfo = new RequestInfo(path);
            Client client = this.clientRegistry.get(requestInfo.endpoint);
            if (client != null) {
                ClientResponse cResponse = this.execRequest(client, requestInfo, resp);
                processDeviceResponse(resp, cResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("no registered client with id '%s'", requestInfo.endpoint).flush();
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append(e.getMessage()).flush();
        } catch (ResourceAccessException e) {
            LOG.debug(String.format("Error accessing resource %s%s.", req.getServletPath(), req.getPathInfo()), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append(e.getMessage()).flush();
        }
    }

    private void processDeviceResponse(HttpServletResponse resp, ClientResponse cResponse) throws IOException {
        String response = null;
        if (cResponse == null) {
            response = "Request timeout";
        } else {
            response = this.gson.toJson(cResponse);
        }
        resp.setContentType("application/json");
        resp.getOutputStream().write(response.getBytes());
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private ClientResponse readRequest(Client client, RequestInfo requestInfo, HttpServletResponse resp) {
        return ReadRequest.newRequest(client, requestInfo.objectId, requestInfo.objectInstanceId,
                requestInfo.resourceId).send(this.requestHandler);
    }

    private ClientResponse observeRequest(Client client, RequestInfo requestInfo, HttpServletResponse resp) {
        ObserveResponse response = (ObserveResponse) ObserveRequest.newRequest(client, this.resourceObserver,
                requestInfo.objectId, requestInfo.objectInstanceId, requestInfo.resourceId).send(this.requestHandler);
        LOG.trace("Observing resource [{}] with observation ID [{}]", requestInfo.toString(),
                response.getObservationId());
        return response;
    }

    private ClientResponse execRequest(Client client, RequestInfo requestInfo, HttpServletResponse resp) {
        return ExecRequest.newRequest(client, requestInfo.objectId, requestInfo.objectInstanceId,
                requestInfo.resourceId).send(this.requestHandler);
    }

    private ClientResponse writeRequest(Client client, RequestInfo requestInfo, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        Map<String, String> parameters = new HashMap<String, String>();
        String contentType = HttpFields.valueParameters(req.getContentType(), parameters);
        if ("text/plain".equals(contentType)) {
            String content = IOUtils.toString(req.getInputStream(), parameters.get("charset"));
            return WriteRequest.newReplaceRequest(client, requestInfo.objectId, requestInfo.objectInstanceId,
                    requestInfo.resourceId, content, ContentFormat.TEXT).send(this.requestHandler);
        } else {
            throw new IllegalArgumentException("content type " + req.getContentType()
                    + " not supported for write requests");
        }
    }

    class RequestInfo {

        final String endpoint;
        final Integer objectId;
        Integer objectInstanceId;
        Integer resourceId;
        Integer resourceInstanceId;
        final String uri;

        /**
         * Build LW request info from URI path
         */
        RequestInfo(String[] path) {

            if (path.length < 3 || path.length > 6) {
                throw new IllegalArgumentException("invalid lightweight M2M path");
            }

            StringBuffer b = new StringBuffer();
            this.endpoint = path[1];
            b.append(this.endpoint);

            try {
                this.objectId = Integer.valueOf(path[2]);
                b.append("/").append(this.objectId);

                if (path.length > 3) {
                    this.objectInstanceId = Integer.valueOf(path[3]);
                    b.append("/").append(this.objectInstanceId);
                }
                if (path.length > 4) {
                    this.resourceId = Integer.valueOf(path[4]);
                    b.append("/").append(this.resourceId);
                }
                if (path.length > 5) {
                    this.resourceInstanceId = Integer.valueOf(path[5]);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid lightweight M2M path", e);
            }
            this.uri = b.toString();
        }

        @Override
        public String toString() {
            return this.uri;
        }
    }
}
