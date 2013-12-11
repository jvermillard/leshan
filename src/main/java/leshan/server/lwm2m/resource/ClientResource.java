package leshan.server.lwm2m.resource;

import java.net.InetAddress;
import java.util.Date;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

/**
 * A {@link Resource} describing a client registered on the server.
 * <p>
 * Each client is identified by a unique path ("/rd/{registrationId}" in the RD resource tree.
 * </p>
 */
public class ClientResource extends ResourceBase implements Client {

    private static final Logger LOG = LoggerFactory.getLogger(ClientResource.class);

    private static final long DEFAULT_LIFETIME_IN_SEC = 86400L;

    private static final String DEFAULT_LWM2M_VERSION = "1.0";

    private final Date registrationDate;

    private InetAddress address;

    private int port;

    private long lifeTimeInSec;

    private String smsNumber;

    private String lwM2mVersion;

    private BindingMode bindingMode;

    public ClientResource(String registrationId, String endpoint, InetAddress address, Integer port) {
        this(registrationId, endpoint, address, port, null, null, null, null);
    }

    public ClientResource(String registrationId, String endpoint, InetAddress address, Integer port,
            String lwM2mVersion, Long lifetime, String smsNumber, BindingMode binding) {
        super(registrationId);

        Validate.notEmpty(endpoint);
        Validate.notNull(address);
        Validate.notNull(port);

        this.getAttributes().addAttribute(LinkFormat.END_POINT, endpoint);

        this.address = address;
        this.port = port;
        this.registrationDate = new Date();
        this.lifeTimeInSec = lifetime == null ? DEFAULT_LIFETIME_IN_SEC : lifetime;
        this.lwM2mVersion = lwM2mVersion == null ? DEFAULT_LWM2M_VERSION : lwM2mVersion;
        this.bindingMode = bindingMode == null ? BindingMode.U : bindingMode;
        this.smsNumber = smsNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleDELETE(CoapExchange exchange) {
        LOG.debug("DEREGISTER for client with registration ID {} and endpoint {}", this.getName(), this.getEndpoint());

        // remove the resource from the tree
        delete();

        exchange.respond(ResponseCode.DELETED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEndpoint() {
        return this.getAttributes().getAttributeValues(LinkFormat.END_POINT).get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRegistrationId() {
        return this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getRegistrationDate() {
        return registrationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress getAddress() {
        return address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLifeTimeInSec() {
        return lifeTimeInSec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSmsNumber() {
        return smsNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLwM2mVersion() {
        return lwM2mVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BindingMode getBindingMode() {
        return bindingMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportObject(String objectId) {
        // TODO
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientResource [registrationDate=").append(registrationDate).append(", address=")
                .append(address).append(", port=").append(port).append(", lifeTimeInSec=").append(lifeTimeInSec)
                .append(", smsNumber=").append(smsNumber).append(", lwM2mVersion=").append(lwM2mVersion)
                .append(", bindingMode=").append(bindingMode).append(", getEndpoint()=").append(getEndpoint())
                .append(", getRegistrationId()=").append(getRegistrationId()).append("]");
        return builder.toString();
    }

}
