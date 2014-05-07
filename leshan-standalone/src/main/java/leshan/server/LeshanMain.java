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
package leshan.server;

import leshan.server.lwm2m.LwM2mServer;
import leshan.server.lwm2m.client.ClientRegistryImpl;
import leshan.server.servlet.ApiServlet;
import leshan.server.servlet.EventServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeshanMain {

    private static final Logger LOG = LoggerFactory.getLogger(LeshanMain.class);

    private Server server;

    public void start() {

        ClientRegistryImpl clientRegistry = new ClientRegistryImpl();

        // LWM2M server
        LwM2mServer lwServer = new LwM2mServer(clientRegistry);
        lwServer.start();
        clientRegistry.start();

        String webPort = System.getenv("PORT");

        if (webPort == null || webPort.isEmpty()) {
            webPort = System.getProperty("PORT");
        }

        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        // root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(this.getClass().getClassLoader().getResource("webapp").toExternalForm());

        // root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        EventServlet eventServlet = new EventServlet(clientRegistry);
        ServletHolder eventServletHolder = new ServletHolder(eventServlet);
        root.addServlet(eventServletHolder, "/event/*");

        ServletHolder apiServletHolder = new ServletHolder(new ApiServlet(lwServer.getRequestHandler(), clientRegistry,
                eventServlet));
        root.addServlet(apiServletHolder, "/api/*");

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
        new LeshanMain().start();
    }

}
