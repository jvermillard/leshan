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
import leshan.server.lwm2m.LwM2mBootstrapServer;
import leshan.server.lwm2m.bootstrap.BootstrapStoreImpl;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapMain {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapMain.class);

    public static void main(String[] args) {

        BootstrapStoreImpl bsStore = new BootstrapStoreImpl();
        SecurityRegistry securityRegistry = new SecurityRegistry();
        // JV: testing bootstrap
        // BootstrapConfig bsConfig = new BootstrapConfig();
        // BootstrapConfig.ServerSecurity ss = new BootstrapConfig.ServerSecurity();
        // ss.bootstrapServer = true;
        // ss.publicKeyOrId = "Bleh".getBytes();
        // ss.secretKey = "S3cr3tm3".getBytes();
        // ss.securityMode = SecurityMode.NO_SEC;
        // ss.uri = "coaps://54.67.9.2";clazz
        // ss.serverId = 1;
        //
        // bsConfig.security.put(0, ss);
        //
        // BootstrapConfig.ServerConfig sc = new BootstrapConfig.ServerConfig();
        // sc.binding = BindingMode.U;
        // sc.shortId = 1;
        // sc.lifetime = 36000;
        // bsConfig.servers.put(0, sc);
        //
        // bsStore.addConfig("testlwm2mclient", bsConfig);
        // use those ENV variables for specifying the interface to be bound for coap and coaps
        String iface = System.getenv("COAPIFACE");
        String ifaces = System.getenv("COAPSIFACE");

        LwM2mBootstrapServer bsServer;

        if (iface == null || iface.isEmpty() || ifaces == null || ifaces.isEmpty()) {
            bsServer = new LwM2mBootstrapServer(bsStore, securityRegistry);
        } else {
            String[] add = iface.split(":");
            String[] adds = ifaces.split(":");

            // user specified the iface to be bound
            bsServer = new LwM2mBootstrapServer(new InetSocketAddress(add[0], Integer.parseInt(add[1])),
                    new InetSocketAddress(adds[0], Integer.parseInt(adds[1])), bsStore, securityRegistry);
        }

        bsServer.start();
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
