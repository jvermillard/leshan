package leshan.server.lwm2m;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import leshan.server.lwm2m.bootstrap.BootstrapStore;
import leshan.server.lwm2m.resource.BootstrapResource;
import leshan.server.lwm2m.security.SecureEndpoint;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.scandium.DTLSConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.Endpoint;
import ch.ethz.inf.vs.californium.server.Server;

/**
 * A Lightweight M2M server, serving bootstrap information on /bs.
 */
public class LwM2mBootstrapServer {

    private final static Logger LOG = LoggerFactory.getLogger(LwM2mBootstrapServer.class);

    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    public static final int PORT = 5683;

    /** IANA assigned UDP port for CoAP with DTLS (so for LWM2M) */
    public static final int PORT_DTLS = 5684;

    private final Server coapServer;

    public LwM2mBootstrapServer(BootstrapStore bsStore, SecurityRegistry securityRegistry) {
        this(new InetSocketAddress((InetAddress) null, PORT), new InetSocketAddress((InetAddress) null, PORT_DTLS),
                bsStore, securityRegistry);

    }

    public LwM2mBootstrapServer(InetSocketAddress localAddress, InetSocketAddress localAddressSecure,
            BootstrapStore bsStore, SecurityRegistry securityRegistry) {
        Validate.notNull(bsStore, "bootstrap store must not be null");

        // init CoAP server
        coapServer = new Server();
        Endpoint endpoint = new CoAPEndpoint(localAddress);
        coapServer.addEndpoint(endpoint);

        // init DTLS server

        DTLSConnector connector = new DTLSConnector(localAddressSecure, null);
        connector.getConfig().setServerPsk(securityRegistry);

        Endpoint secureEndpoint = new SecureEndpoint(connector);
        coapServer.addEndpoint(secureEndpoint);

        // define /bs ressource
        BootstrapResource bsResource = new BootstrapResource(bsStore);
        coapServer.add(bsResource);
    }

    /**
     * Starts the server and binds it to the specified port.
     */
    public void start() {
        coapServer.start();
        LOG.info("LW-M2M server started");
    }

    /**
     * Stops the server and unbinds it from assigned ports (can be restarted).
     */
    public void stop() {
        coapServer.stop();
    }

    /**
     * Stops the server and unbinds it from assigned ports.
     */
    public void destroy() {
        coapServer.destroy();
    }
}