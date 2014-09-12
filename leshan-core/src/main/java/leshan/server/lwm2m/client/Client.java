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
package leshan.server.lwm2m.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang.Validate;

/**
 * A LW-M2M client registered on the server
 */
public class Client {

    private static final long DEFAULT_LIFETIME_IN_SEC = 86400L;

    private static final String DEFAULT_LWM2M_VERSION = "1.0";

    private final Date registrationDate;

    private InetAddress address;

    private int port;

    /*
     * The address of the LWM2M Server's CoAP end point the client used to register.
     */
    private final InetSocketAddress registrationEndpointAddress;

    private long lifeTimeInSec;

    private String smsNumber;

    private final String lwM2mVersion;

    private BindingMode bindingMode;

    /**
     * The LWM2M Client's unique end point name.
     */
    private final String endpoint;

    private final String registrationId;

    private LinkObject[] objectLinks;

    private Date lastUpdate;

    // true, if the client failed to answer the last server request
    private boolean failedLastRequest = false;

    public Client(String registrationId, String endpoint, InetAddress address, int port,
            InetSocketAddress registrationEndpoint) {
        this(registrationId, endpoint, address, port, null, null, null, null, null, registrationEndpoint);
    }

    public Client(String registrationId, String endpoint, InetAddress address, int port, String lwM2mVersion,
            Long lifetime, String smsNumber, BindingMode binding, LinkObject[] objectLinks,
            InetSocketAddress registrationEndpoint) {
        this(registrationId, endpoint, address, port, lwM2mVersion, lifetime, smsNumber, binding, objectLinks, null,
                registrationEndpoint);
    }

    public Client(String registrationId, String endpoint, InetAddress address, int port, String lwM2mVersion,
            Long lifetime, String smsNumber, BindingMode binding, LinkObject[] objectLinks, Date registrationDate,
            InetSocketAddress registrationEndpoint) {

        Validate.notEmpty(endpoint);
        Validate.notNull(address);
        Validate.notNull(port);
        Validate.notNull(registrationEndpoint);

        this.registrationId = registrationId;
        this.endpoint = endpoint;
        this.address = address;
        this.port = port;
        this.objectLinks = objectLinks;

        this.registrationDate = registrationDate == null ? new Date() : registrationDate;
        lifeTimeInSec = lifetime == null ? DEFAULT_LIFETIME_IN_SEC : lifetime;
        this.lwM2mVersion = lwM2mVersion == null ? DEFAULT_LWM2M_VERSION : lwM2mVersion;
        bindingMode = binding == null ? BindingMode.U : binding;
        this.smsNumber = smsNumber;
        lastUpdate = new Date();
        registrationEndpointAddress = registrationEndpoint;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Gets the client's network address.
     * 
     * @return the source address from the client's most recent CoAP message.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Gets the client's network port number.
     * 
     * @return the source port from the client's most recent CoAP message.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the network address and port number of LWM2M Server's CoAP endpoint the client originally registered at.
     * 
     * A LWM2M Server may listen on multiple CoAP end points, e.g. a non-secure and a secure one. Clients are often
     * behind a firewall which will only let incoming UDP packets pass if they originate from the same address:port that
     * the client has initiated communication with, e.g. by means of registering with the LWM2M Server. It is therefore
     * important to know, which of the server's CoAP end points the client contacted for registration.
     * 
     * This information can be used to uniquely identify the CoAP endpoint that should be used to access resources on
     * the client.
     * 
     * @return the network address and port number
     */
    public InetSocketAddress getRegistrationEndpointAddress() {
        return registrationEndpointAddress;
    }

    public LinkObject[] getObjectLinks() {
        // sort the list of objects
        if (objectLinks == null) {
            return null;
        }
        LinkObject[] res = Arrays.copyOf(objectLinks, objectLinks.length);

        Arrays.sort(res, new Comparator<LinkObject>() {

            /* sort by objectid, object instance and ressource */
            @Override
            public int compare(LinkObject o1, LinkObject o2) {
                if (o1 == null && o2 == null)
                    return 0;
                if (o1 == null)
                    return -1;
                if (o2 == null)
                    return 1;
                // by object
                Integer oi1 = o1.getObjectId();
                Integer oi2 = o2.getObjectId();

                if (oi1 == null && oi2 == null) {
                    return 0;
                }
                if (oi1 == null) {
                    return -1;
                }
                if (oi2 == null) {
                    return 1;
                }
                int oicomp = oi1.compareTo(oi2);
                if (oicomp != 0) {
                    return oicomp;
                }

                Integer oii1 = o1.getObjectInstanceId();
                Integer oii2 = o2.getObjectInstanceId();
                if (oii1 == null && oii2 == null) {
                    return 0;
                }
                if (oii1 == null) {
                    return -1;
                }
                if (oii2 == null) {
                    return 1;
                }
                int oiicomp = oii1.compareTo(oii2);
                if (oiicomp != 0) {
                    return oiicomp;
                }

                Integer or1 = o1.getResourceId();
                Integer or2 = o2.getResourceId();
                if (or1 == null && or2 == null) {
                    return 0;
                }
                if (or1 == null) {
                    return -1;
                }
                if (or2 == null) {
                    return 1;
                }
                return or1.compareTo(or2);

            }
        });

        return res;
    }

    void setObjectLinks(LinkObject[] objectLinks) {
        this.objectLinks = objectLinks;
    }

    public synchronized long getLifeTimeInSec() {
        return lifeTimeInSec;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public String getLwM2mVersion() {
        return lwM2mVersion;
    }

    public BindingMode getBindingMode() {
        return bindingMode;
    }

    /**
     * Gets the unique name the client has registered with.
     * 
     * @return the name
     */
    public String getEndpoint() {
        return endpoint;
    }

    void setAddress(InetAddress address) {
        this.address = address;
    }

    void setPort(int port) {
        this.port = port;
    }

    void setLifeTimeInSec(long lifeTimeInSec) {
        this.lifeTimeInSec = lifeTimeInSec;
    }

    void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    void setBindingMode(BindingMode bindingMode) {
        this.bindingMode = bindingMode;
    }

    public synchronized Date getLastUpdate() {
        return lastUpdate;
    }

    synchronized void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public synchronized void markLastRequestFailed() {
        failedLastRequest = true;
    }

    public synchronized boolean isMarkLastRequestFailed() {
        return failedLastRequest;
    }

    public synchronized boolean isAlive() {
        return failedLastRequest ? false : lastUpdate.getTime() + lifeTimeInSec * 1000 > System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String
                .format("Client [registrationDate=%s, address=%s, port=%s, registrationEndpoint=%s, lifeTimeInSec=%s, smsNumber=%s, lwM2mVersion=%s, bindingMode=%s, endpoint=%s, registrationId=%s, objectLinks=%s, lastUpdate=%s, failedLastRequest=%s]",
                        registrationDate, address, port, registrationEndpointAddress, lifeTimeInSec, smsNumber,
                        lwM2mVersion, bindingMode, endpoint, registrationId, Arrays.toString(objectLinks), lastUpdate,
                        failedLastRequest);
    }

    /**
     * Computes a hash code for this client.
     * 
     * @return the hash code based on the <em>endpoint</em> property
     */
    @Override
    public int hashCode() {
        return getEndpoint().hashCode();
    }

    /**
     * Compares this Client to another object.
     * 
     * @return <code>true</code> if the other object is a Client instance and its <em>endpoint</em> property has the
     *         same value as this Client
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Client) {
            Client other = (Client) obj;
            return this.getEndpoint().equals(other.getEndpoint());
        } else {
            return false;
        }
    }
}
