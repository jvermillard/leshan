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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.RegistryListener;
import leshan.server.servlet.json.ClientSerializer;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EventServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(EventServlet.class);

    private final Gson gson;

    private final byte[] EVENT = "event: ".getBytes();

    private final byte[] DATA = "data: ".getBytes();

    private final byte[] VOID = ": ".getBytes();

    private static final byte[] TERMINATION = new byte[] { '\r', '\n' };

    public EventServlet(ClientRegistry registry) {
        registry.addListener(listener);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Client.class, new ClientSerializer());
        gson = gsonBuilder.create();
    }

    private Set<Continuation> continuations = new ConcurrentHashSet<>();

    private RegistryListener listener = new RegistryListener() {

        @Override
        public void registered(Client client) {
            sendEvent("REGISTRATION", client);
        }

        public void updated(Client clientUpdated) {
            sendEvent("UPDATED", clientUpdated);
        };

        @Override
        public void unregistered(Client client) {
            sendEvent("DEREGISTRATION", client);
        }
    };

    private void sendEvent(String event, Client client) {
        LOG.debug("Registration event {} for client {}", event, client);

        Collection<Continuation> disconnected = new ArrayList<>();
        String jClient = gson.toJson(client);

        for (Continuation c : continuations) {
            try {
                OutputStream output = c.getServletResponse().getOutputStream();
                output.write(EVENT);
                output.write(event.getBytes("UTF-8"));
                output.write(TERMINATION);
                output.write(DATA);
                output.write(jClient.getBytes("UTF-8"));
                output.write(TERMINATION);
                output.write(TERMINATION);
                output.flush();
                c.getServletResponse().flushBuffer();
            } catch (IOException e) {
                LOG.debug("Disconnected SSE client");
                disconnected.add(c);
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
        continuations.add(c);
        c.suspend(resp);
    }
}