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
package leshan.standalone.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leshan.server.LwM2mServer;
import leshan.server.client.Client;
import leshan.server.client.ClientRegistryListener;
import leshan.server.node.LwM2mNode;
import leshan.server.observation.Observation;
import leshan.server.observation.ObservationRegistryListener;
import leshan.standalone.servlet.json.ClientSerializer;
import leshan.standalone.servlet.json.LwM2mNodeSerializer;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EventServlet extends HttpServlet {

    private static final String EVENT_DEREGISTRATION = "DEREGISTRATION";

    private static final String EVENT_UPDATED = "UPDATED";

    private static final String EVENT_REGISTRATION = "REGISTRATION";

    private static final String EVENT_NOTIFICATION = "NOTIFICATION";

    private static final String QUERY_PARAM_ENDPOINT = "ep";

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(EventServlet.class);

    private final Gson gson;

    private final byte[] EVENT = "event: ".getBytes();

    private final byte[] DATA = "data: ".getBytes();

    private final byte[] VOID = ": ".getBytes();

    private static final byte[] TERMINATION = new byte[] { '\r', '\n' };

    private final Set<Continuation> continuations = new ConcurrentHashSet<>();

    private final ClientRegistryListener clientRegistryListener = new ClientRegistryListener() {

        @Override
        public void registered(Client client) {
            String jClient = EventServlet.this.gson.toJson(client);
            sendEvent(EVENT_REGISTRATION, jClient, client.getEndpoint());
        }

        @Override
        public void updated(Client clientUpdated) {
            String jClient = EventServlet.this.gson.toJson(clientUpdated);
            sendEvent(EVENT_UPDATED, jClient, clientUpdated.getEndpoint());
        };

        @Override
        public void unregistered(Client client) {
            String jClient = EventServlet.this.gson.toJson(client);
            sendEvent(EVENT_DEREGISTRATION, jClient, client.getEndpoint());
        }
    };

    private final ObservationRegistryListener observationRegistryListener = new ObservationRegistryListener() {

        @Override
        public void cancelled(Observation observation) {
        }

        @Override
        public void newValue(Observation observation, LwM2mNode value) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received notification from [{}] containing value [{}]", observation.getPath(),
                        value.toString());
            }
            String data = new StringBuffer("{\"ep\":\"").append(observation.getClient().getEndpoint())
                    .append("\",\"res\":\"").append(observation.getPath().toString()).append("\",\"val\":")
                    .append(gson.toJson(value)).append("}").toString();

            sendEvent(EVENT_NOTIFICATION, data, observation.getClient().getEndpoint());
        }

        @Override
        public void newObservation(Observation observation) {
        }
    };

    public EventServlet(LwM2mServer server) {
        server.getClientRegistry().addListener(this.clientRegistryListener);
        server.getObservationRegistry().addListener(this.observationRegistryListener);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Client.class, new ClientSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeSerializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        this.gson = gsonBuilder.create();
    }

    private synchronized void sendEvent(String event, String data, String endpoint) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching {} event from endpoint {}", event, endpoint);
        }

        Collection<Continuation> disconnected = new ArrayList<>();

        for (Continuation c : continuations) {
            Object endpointAttribute = c.getAttribute(QUERY_PARAM_ENDPOINT);
            if (endpointAttribute == null || endpointAttribute.equals(endpoint)) {
                try {
                    OutputStream output = c.getServletResponse().getOutputStream();
                    output.write(EVENT);
                    output.write(event.getBytes("UTF-8"));
                    output.write(TERMINATION);
                    output.write(DATA);
                    output.write(data.getBytes("UTF-8"));
                    output.write(TERMINATION);
                    output.write(TERMINATION);
                    output.flush();
                    c.getServletResponse().flushBuffer();
                } catch (IOException e) {
                    LOG.debug("Disconnected SSE client");
                    disconnected.add(c);
                }
            }
        }
        continuations.removeAll(disconnected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/event-stream");
        OutputStream output = resp.getOutputStream();
        output.write(VOID);
        output.write("waiting for events".getBytes());
        output.write(TERMINATION);
        output.flush();
        resp.flushBuffer();

        Continuation c = ContinuationSupport.getContinuation(req);
        c.setTimeout(0);
        c.addContinuationListener(new ContinuationListener() {

            @Override
            public void onTimeout(Continuation continuation) {
                LOG.debug("continuation closed");
                continuation.complete();
            }

            @Override
            public void onComplete(Continuation continuation) {
                LOG.debug("continuation completed");
                continuations.remove(continuation);
            }
        });

        String endpoint = req.getParameter(QUERY_PARAM_ENDPOINT);
        if (endpoint != null) {
            // mark continuation as notification listener for endpoint
            c.setAttribute(QUERY_PARAM_ENDPOINT, endpoint);
        }
        synchronized (this) {
            continuations.add(c);
            c.suspend(resp);
        }
    }
}
