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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leshan.server.lwm2m.session.Session;
import leshan.server.lwm2m.session.SessionRegistry;

/**
 * Service HTTP REST API calls.
 */
public class ApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final SessionRegistry registry;
    
    public ApiServlet(SessionRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       String path = req.getPathInfo();

       switch (path) {
        case "/clients":
            for(Session session  : registry.allSessions()) {
                resp.getOutputStream().write(("client : "+session.getRegistrationId()+"\n").getBytes());
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        default:
            resp.getOutputStream().write(("not found: '"+path+"'").getBytes());
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
       
    }
}