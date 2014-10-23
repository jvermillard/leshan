/*
 * Copyright (c) 2013, Sierra Wireless
 * Copyright (c) 2014, Zebra Technologies
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

import java.net.InetSocketAddress;
import java.util.Collection;

import leshan.connector.californium.resource.CaliforniumCoapResourceProxy;
import leshan.connector.californium.server.CaliforniumServerImplementor;
import leshan.connector.californium.server.CaliforniumServerBuilder;
import leshan.server.lwm2m.LeshanServer;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistrationException;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.ClientRegistryListener;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.impl.ClientRegistryImpl;
import leshan.server.lwm2m.impl.ObservationRegistryImpl;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementor;
import leshan.server.lwm2m.impl.security.SecurityRegistryImpl;
import leshan.server.servlet.ClientServlet;
import leshan.server.servlet.EventServlet;
import leshan.server.servlet.SecurityServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeshanMain {

    private static final Logger LOG = LoggerFactory.getLogger(LeshanMain.class);

    private Server server;
    private LeshanServer lwServer;

    public void start() {
        // use those ENV variables for specifying the interface to be bound for coap and coaps
        String iface = System.getenv("COAPIFACE");
        String ifaces = System.getenv("COAPSIFACE");

        // LWM2M server
        CaliforniumServerBuilder serverSchematics = new CaliforniumServerBuilder();

        if (iface == null || iface.isEmpty() || ifaces == null || ifaces.isEmpty()) {
        	serverSchematics.addEndpoint(new InetSocketAddress(CoapServerImplementor.PORT));
        	serverSchematics.addSecureEndpoint(new InetSocketAddress(CoapServerImplementor.PORT_DTLS));
        } else {
            String[] add = iface.split(":");
            String[] adds = ifaces.split(":");
            // user specified the iface to be bound
            serverSchematics.addEndpoint(new InetSocketAddress(add[0], Integer.parseInt(add[1])));
            serverSchematics.addSecureEndpoint(new InetSocketAddress(adds[0], Integer.parseInt(adds[1])));
        }
        
        CoapServerImplementor implementor = serverSchematics.setClientRegistry(new ClientRegistryImpl())
        													.setObservationRegistry(new ObservationRegistryImpl())
        													.setSecurityRegistry(new SecurityRegistryImpl())
        													.bindResource(new CaliforniumCoapResourceProxy())
        													.build();
        
        lwServer = new LeshanServer(implementor);
        lwServer.start();

        // now prepare and start jetty
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
        root.setResourceBase(this.getClass().getClassLoader().getResource("webapp").toExternalForm());
        root.setParentLoaderPriority(true);
        server.setHandler(root);

        // Create Servlet
        EventServlet eventServlet = new EventServlet(lwServer);
        ServletHolder eventServletHolder = new ServletHolder(eventServlet);
        root.addServlet(eventServletHolder, "/event/*");

        ServletHolder clientServletHolder = new ServletHolder(new ClientServlet(lwServer));
        root.addServlet(clientServletHolder, "/api/clients/*");

        ServletHolder securityServletHolder = new ServletHolder(new SecurityServlet(lwServer.getSecurityRegistry()));
        root.addServlet(securityServletHolder, "/api/security/*");

        // Start jetty
        try {
            server.start();
        } catch (Exception e) {
            LOG.error("jetty error", e);
        }
    }

    public void stop() {
        try {
            lwServer.destroy();
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new LeshanMain().start();
    }
}