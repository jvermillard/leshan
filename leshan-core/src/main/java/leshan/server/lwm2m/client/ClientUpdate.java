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
import java.util.Arrays;
import java.util.Date;

/**
 * A container object for updating a LW-M2M client's registration properties on
 * the server.
 * 
 * According to the LWM2M spec only those properties need to be set that have
 * changed and thus should be updated on the server.
 */
public class ClientUpdate {

    private final InetAddress address;

    private final Integer port;

    private final Long lifeTimeInSec;

    private final String smsNumber;

    private final BindingMode bindingMode;

    private final String registrationId;

    private final String[] objectLinks;

    public ClientUpdate(String registrationId, InetAddress address, Integer port) {
        this(registrationId, address, port, null, null, null, null);
    }

    public ClientUpdate(String registrationId, InetAddress address, Integer port, Long lifetime,
                        String smsNumber, BindingMode binding, String[] objectLinks) {
        this(registrationId, address, port, lifetime, smsNumber, binding, objectLinks, null);
    }

    /**
     * Sets all fields.
     * 
     * @param registrationId the ID under which the client is registered
     * @param address the client's host name or IP address
     * @param port the UDP port the client uses for communication
     * @param lifetime the number of seconds the client would like its
     *            registration to be valid
     * @param smsNumber the SMS number the client can receive messages under
     * @param binding the binding mode(s) the client supports
     * @param objectLinks the objects and object instances the client
     *            hosts/supports
     * @param registrationDate the point in time the client registered with the
     *            server (?)
     * @throws NullPointerException if the registration ID is <code>null</code>
     */
    public ClientUpdate(String registrationId, InetAddress address, Integer port, Long lifetime,
                        String smsNumber, BindingMode binding, String[] objectLinks, Date registrationDate) {

        if (registrationId == null) {
            throw new NullPointerException("Registration ID must not be null");
        }
        this.registrationId = registrationId;
        this.address = address;
        this.port = port;
        this.objectLinks = objectLinks;
        this.lifeTimeInSec = lifetime;
        this.bindingMode = binding;
        this.smsNumber = smsNumber;
    }

    public String getRegistrationId() {
        return this.registrationId;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public Integer getPort() {
        return this.port;
    }

    public String[] getObjectLinks() {
        return this.objectLinks;
    }

    public Long getLifeTimeInSec() {
        return this.lifeTimeInSec;
    }

    public String getSmsNumber() {
        return this.smsNumber;
    }

    public BindingMode getBindingMode() {
        return this.bindingMode;
    }

    /**
     * Applies the registration property changes to a given client registration object.
     * 
     * @param client the registration to apply the changes to
     * @throws NullPointerException if the given client registration is <code>null</code>
     */
    public void apply(Client client) {

        if (client == null) {
            throw new NullPointerException("Client must not be null");
        }

        if (getAddress() != null) {
            client.setAddress(getAddress());
        }

        if (getPort() != null) {
            client.setPort(getPort());
        }

        if (getObjectLinks() != null) {
            client.setObjectLinks(getObjectLinks());
        }

        if (getLifeTimeInSec() != null) {
            client.setLifeTimeInSec(getLifeTimeInSec());
        }

        if (getBindingMode() != null) {
            client.setBindingMode(getBindingMode());
        }

        if (getSmsNumber() != null) {
            client.setSmsNumber(getSmsNumber());
        }

        // this needs to be done in any case, even if no properties have changed, in order
        // to extend the client registration's time-to-live period ...
        client.setLastUpdate(new Date());
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("ClientUpdate [address=").append(this.address);
        b.append(", port=").append(this.port);
        b.append(", lifeTimeInSec=").append(this.lifeTimeInSec);
        b.append(", smsNumber=").append(this.smsNumber);
        b.append(", bindingMode=").append(this.bindingMode);
        b.append(", registrationId=").append(this.registrationId);
        b.append(", objectLinks=").append(Arrays.toString(this.objectLinks));
        b.append("]");
        return b.toString();
    }
}
