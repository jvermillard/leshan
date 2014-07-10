package leshan.server.lwm2m.security;

import java.net.InetSocketAddress;

import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.dtls.DTLSSession;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;

/**
 * A {@link CoAPEndpoint} for communications using DTLS security.
 */
public class SecureEndpoint extends CoAPEndpoint {

    private final DTLSConnector connector;

    public SecureEndpoint(DTLSConnector connector) {
        super(connector, NetworkConfig.getStandard());
        this.connector = connector;
    }

    /**
     * Returns the PSK identity from the DTLS session associated with the given request.
     * 
     * @param request the CoAP request
     * @return the PSK identity of the client of <code>null</code> if not found.
     */
    public String getPskIdentity(Request request) {
        return this.getSession(request).getPskIdentity();
    }

    private DTLSSession getSession(Request request) {
        return connector.getSessionByAddress(new InetSocketAddress(request.getSource(), request.getSourcePort()));
    }

}
