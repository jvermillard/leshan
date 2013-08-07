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
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leshan.server.lwm2m.session.LwSession;
import leshan.server.lwm2m.session.RegistryListener;
import leshan.server.lwm2m.session.SessionRegistry;
import leshan.server.servlet.json.Client;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class EventServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(EventServlet.class);
    
    private final Gson gson = new Gson();

    public EventServlet(SessionRegistry registry) {
        registry.addListener(listener);
        
    }
    
    private Set<Continuation> continuations = new  ConcurrentHashSet<>();
    
    private RegistryListener listener = new RegistryListener() {
        
        @Override
        public void registered(LwSession session) {
            for(Continuation c:continuations) {
                try {
                Client client = new Client(session.getEndpoint(), session.getRegistrationId(),session.getIoSession().getRemoteAddress().toString(),session.getObjects(),session.getSmsNumber(),session.getLwM2mVersion(),session.getLifeTimeInSec());
                    c.getServletResponse().getWriter().write(gson.toJson(client));
                    c.getServletResponse().getWriter().flush();
                } catch (IOException e) {
                    LOG.error("Exception",e);
                }
            }
        }
        
        @Override
        public void unregistered(LwSession session) {
            
        }
    };
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ServletException {
        super.init();
        
        
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final Continuation c= ContinuationSupport.getContinuation(req);
        c.suspend(resp);
        
    }
}
