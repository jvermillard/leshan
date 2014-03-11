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
package leshan.server;

import leshan.server.lwm2m.CoapServer;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.ClientRegistryImpl;
import leshan.server.servlet.ApiServlet;
import leshan.server.servlet.EventServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LwM2mServer {

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mServer.class);

    private Server server;

    public void start() {

        ClientRegistry clientRegistry = new ClientRegistryImpl();

        // LWM2M server
        CoapServer lwServer = new CoapServer(clientRegistry);
        lwServer.start();

        // now prepare and start jetty
        String webappDirLocation = "src/main/webapp/";

        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        ServletHolder apiServletHolder = new ServletHolder(new ApiServlet(lwServer.getRequestHandler(), clientRegistry));
        root.addServlet(apiServletHolder, "/api/*");

        ServletHolder eventServletHolder = new ServletHolder(new EventServlet(clientRegistry));
        root.addServlet(eventServletHolder, "/event/*");

        server.setHandler(root);

        try {
            server.start();
        } catch (Exception e) {
            LOG.error("jetty error", e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new LwM2mServer().start();
    }

}
