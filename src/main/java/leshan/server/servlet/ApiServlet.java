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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leshan.server.lwm2m.LwM2mRequestFilter;
import leshan.server.lwm2m.message.client.ClientResponse;
import leshan.server.lwm2m.message.client.ContentResponse;
import leshan.server.lwm2m.message.server.ReadRequest;
import leshan.server.lwm2m.session.LwSession;
import leshan.server.lwm2m.session.SessionRegistry;
import leshan.server.servlet.json.Client;
import leshan.server.servlet.json.ReadResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.mina.api.IoFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Service HTTP REST API calls.
 */
public class ApiServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ApiServlet.class);

    private static final long serialVersionUID = 1L;

    private final SessionRegistry registry;

    private final LwM2mRequestFilter requestFilter;

    private final Gson gson = new Gson();

    public ApiServlet(SessionRegistry registry, LwM2mRequestFilter requestFilter) {
        this.registry = registry;
        this.requestFilter = requestFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String[] path = StringUtils.split(req.getPathInfo(), '/');

        if (ArrayUtils.isEmpty(path)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            switch (path[0]) {
            case "clients":
                if (path.length == 1) {
                    // list registered clients
                    List<Client> clients = new ArrayList<>();
                    for (LwSession session : registry.allSessions()) {
                        clients.add(new Client(session.getEndpoint(), session.getRegistrationId(), session
                                .getIoSession().getRemoteAddress().toString(), session.getObjects(), session
                                .getSmsNumber(), session.getLwM2mVersion(), session.getLifeTimeInSec()));
                    }

                    String json = gson.toJson(clients.toArray(new Client[] {}));
                    resp.setContentType("application/json");
                    resp.getOutputStream().write(json.getBytes());

                    resp.setStatus(HttpServletResponse.SC_OK);
                    return;
                } else {
                    // READ resource
                    if (path.length < 3) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    } else {
                        String endpoint = path[1];
                        LwSession session = registry.getSession(endpoint);
                        if (session == null) {
                            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no registered client with id '"
                                    + endpoint + "'");
                            return;
                        } else {
                            this.readRequest(session, path, resp);
                            return;
                        }
                    }
                }
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "not found: '" + req.getPathInfo() + "'");
                return;
            }
        } catch (Exception e) {
            LOG.error("unexpected error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private void readRequest(LwSession session, String[] path, HttpServletResponse resp) throws InterruptedException,
            ExecutionException, IOException {

        Integer objectId = Integer.valueOf(path[2]);
        Integer objectInstanceId = null;
        Integer resourceId = null;

        if (path.length > 3) {
            objectInstanceId = Integer.valueOf(path[3]);
        }
        if (path.length > 4) {
            resourceId = Integer.valueOf(path[4]);
        }

        ReadRequest request = new ReadRequest(objectId, objectInstanceId, resourceId);
        IoFuture<ClientResponse> future = requestFilter.request(session.getIoSession(), request, 5000);
        // wait for client response
        ClientResponse lwResponse = future.get();

        // build JSON read response
        String value = null;
        if (lwResponse instanceof ContentResponse) {
            ContentResponse cResponse = (ContentResponse) lwResponse;
            switch (cResponse.getFormat()) {
            case TLV:
                value = "TLV : " + Hex.encodeHexString(cResponse.getContent());
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

        ReadResponse response = new ReadResponse(lwResponse.getCode().toString(), value);

        String json = gson.toJson(response);
        resp.setContentType("application/json");
        resp.getOutputStream().write(json.getBytes());

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}