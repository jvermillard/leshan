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

package leshan.bootstrap;

import java.net.InetSocketAddress;

import leshan.bootstrap.servlet.BootstrapServlet;
import leshan.connector.californium.bootstrap.BootstrapServerImplementor;
import leshan.connector.californium.bootstrap.CaliforniumBssSchematic;
import leshan.connector.californium.resource.CaliforniumCoapResourceProxy;
import leshan.server.lwm2m.impl.LwM2mBootstrapServerImpl;
import leshan.server.lwm2m.impl.bridge.bootstrap.BootstrapImplementor;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementor;
import leshan.server.lwm2m.security.SecurityStore;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapMain {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapMain.class);

    public static void main(String[] args) {

        BootstrapStoreImpl bsStore = new BootstrapStoreImpl();
        SecurityStore securityStore = new BoostrapSecurityStore(bsStore);

        // use those ENV variables for specifying the interface to be bound for coap and coaps
        String iface = System.getenv("COAPIFACE");
        String ifaces = System.getenv("COAPSIFACE");

        CaliforniumBssSchematic serverSchematics = new CaliforniumBssSchematic();

        if (iface == null || iface.isEmpty() || ifaces == null || ifaces.isEmpty()) {
        	serverSchematics.addEndpoint(new InetSocketAddress(BootstrapImplementor.PORT));
        	serverSchematics.addSecureEndpoint(new InetSocketAddress(BootstrapImplementor.PORT_DTLS));
        } else {
            String[] add = iface.split(":");
            String[] adds = ifaces.split(":");

            serverSchematics.addEndpoint(new InetSocketAddress(add[0], Integer.parseInt(add[1])));
            serverSchematics.addSecureEndpoint(new InetSocketAddress(adds[0], Integer.parseInt(adds[1])));
        }
        
        BootstrapServerImplementor implementor = serverSchematics.setBootstrapStore(bsStore)
        										.setSecurityStore(securityStore)
        										.bindResource(new CaliforniumCoapResourceProxy()).build();
        
        LwM2mBootstrapServerImpl impl = new LwM2mBootstrapServerImpl(implementor);
        impl.start();
        
        // now prepare and start jetty

        String webPort = System.getenv("PORT");

        if (webPort == null || webPort.isEmpty()) {
            webPort = System.getProperty("PORT");
        }

        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        Server server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        // root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(BootstrapMain.class.getClassLoader().getResource("webapp").toExternalForm());

        // root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        ServletHolder bsServletHolder = new ServletHolder(new BootstrapServlet(bsStore));
        root.addServlet(bsServletHolder, "/api/bootstrap/*");

        server.setHandler(root);

        try {
            server.start();
        } catch (Exception e) {
            LOG.error("jetty error", e);
        }

    }
}
