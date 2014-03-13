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
