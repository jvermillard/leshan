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
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResourceSpec;
import leshan.server.servlet.json.ClientSerializer;
import leshan.server.event.EventDispatcher;
import leshan.server.event.EventListener;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EventServlet extends HttpServlet implements EventListener {

    public enum EventType {
      EVENT_DEREGISTRATION("DEREGISTRATION"),
      EVENT_UPDATED("UPDATED"),
      EVENT_REGISTRATION("REGISTRATION"),
      EVENT_NOTIFICATION("NOTIFICATION"),
      ;
      
      private EventType( String representation) {
        this.representation = representation;
      }
      
      public final String representation;
      
    }
    
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(EventServlet.class);

    private static final String QUERY_PARAM_ENDPOINT = "ep";

    private final Gson gson;

    private final byte[] EVENT = "event: ".getBytes();

    private final byte[] DATA = "data: ".getBytes();

    private final byte[] VOID = ": ".getBytes();

    private static final byte[] TERMINATION = new byte[] { '\r', '\n' };

    private final Set<Continuation> continuations = new ConcurrentHashSet<>();

    public EventServlet() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Client.class, new ClientSerializer());
        this.gson = gsonBuilder.create();
        EventDispatcher.getInstance().addEventListener(this);
    }

    @Override
    public void registered(Client client) {
      String jClient = EventServlet.this.gson.toJson(client);

      sendEvent(EventType.EVENT_REGISTRATION, jClient, client.getEndpoint());
    }

    @Override
    public void updated(Client clientUpdated) {
      String jClient = EventServlet.this.gson.toJson(clientUpdated);

      sendEvent(EventType.EVENT_UPDATED, jClient, clientUpdated.getEndpoint());
    }

    @Override
    public void unregistered(Client client) {
      String jClient = EventServlet.this.gson.toJson(client);
      sendEvent(EventType.EVENT_DEREGISTRATION, jClient, client.getEndpoint());
    }

    @Override
    public void notify(byte[] content, ContentFormat contentFormat, ResourceSpec target) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received notification from [{}] containing value [{}]", target, new String(content));
        }

        String data = null;
        switch (contentFormat) {
        case OPAQUE:
            // TODO: handle binary data
            LOG.debug("Binary data not supported yet");
            return;
        case TLV:
            // TODO: decode TLV
            LOG.debug("TLV encoded data not supported yet");
            return;
        case JSON:
            // TODO: handle JSON data
            LOG.debug("JSON encoded data not supported yet");
            return;
        case TEXT:
        default:
            data = new StringBuffer("{\"ep\":\"").append(target.getClient().getEndpoint()).append("\",\"res\":\"")
                    .append(target.asRelativePath()).append("\",\"val\":\"").append(new String(content)).append("\"}")
                    .toString();

        }
        sendEvent(EventType.EVENT_NOTIFICATION, data, target.getClient().getEndpoint());
    }

    private synchronized void sendEvent(EventType eventType, String data, String endpoint) {
        String event = eventType.representation;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching {} event from endpoint {}", event, endpoint);
        }
        
        Collection<Continuation> disconnected = new ArrayList<>();
        for (Continuation c : continuations) {
            if (endpoint.equals(c.getAttribute(QUERY_PARAM_ENDPOINT)) || !EventType.EVENT_NOTIFICATION.equals(eventType)) {
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
