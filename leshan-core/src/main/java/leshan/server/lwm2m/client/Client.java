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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.Validate;

/**
 * A LW-M2M client registered on the server.
 * 
 */
public class Client implements Serializable {

    /**
     * Serializable in order to support (distributed) caching, e.g. via JSR 107
     */
    private static final long serialVersionUID = 1L;

    private static final long DEFAULT_LIFETIME_IN_SEC = 86400L;

    private static final String DEFAULT_LWM2M_VERSION = "1.0";

    private final Date registrationDate;

    private InetAddress address;

    private int port;

    private long lifeTimeInSec;

    private String smsNumber;

    private String lwM2mVersion;

    private BindingMode bindingMode;

    private final String endpoint;

    private final String registrationId;

    private String[] objectLinks;

    private Date lastUpdate;

    // does the client failed to answer the last server request
    private boolean failedLastRequest = false;

    public Client(String registrationId, String endpoint, InetAddress address, int port) {
        this(registrationId, endpoint, address, port, null, null, null, null, null);
    }

    public Client(String registrationId, String endpoint, InetAddress address, int port, String lwM2mVersion,
            Long lifetime, String smsNumber, BindingMode binding, String[] objectLinks) {
        this(registrationId, endpoint, address, port, lwM2mVersion, lifetime, smsNumber, binding, objectLinks, null);
    }

    public Client(String registrationId, String endpoint, InetAddress address, int port, String lwM2mVersion,
            Long lifetime, String smsNumber, BindingMode binding, String[] objectLinks, Date registrationDate) {

        Validate.notEmpty(endpoint);
        Validate.notNull(address);
        Validate.notNull(port);

        this.registrationId = registrationId;
        this.endpoint = endpoint;
        this.address = address;
        this.port = port;
        this.objectLinks = objectLinks;
        this.registrationDate = registrationDate == null ? new Date() : registrationDate;
        this.lifeTimeInSec = lifetime == null ? DEFAULT_LIFETIME_IN_SEC : lifetime;
        this.lwM2mVersion = lwM2mVersion == null ? DEFAULT_LWM2M_VERSION : lwM2mVersion;
        this.bindingMode = binding == null ? BindingMode.U : binding;
        this.smsNumber = smsNumber;
        this.lastUpdate = new Date();
    }

    public String getRegistrationId() {
        return this.registrationId;
    }

    public Date getRegistrationDate() {
        return this.registrationDate;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public String[] getObjectLinks() {
        return this.objectLinks;
    }

    public void setObjectLinks(String[] objectLinks) {
        this.objectLinks = objectLinks;
    }

    public long getLifeTimeInSec() {
        return this.lifeTimeInSec;
    }

    public String getSmsNumber() {
        return this.smsNumber;
    }

    public String getLwM2mVersion() {
        return this.lwM2mVersion;
    }

    public BindingMode getBindingMode() {
        return this.bindingMode;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setLifeTimeInSec(long lifeTimeInSec) {
        this.lifeTimeInSec = lifeTimeInSec;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public void setLwM2mVersion(String lwM2mVersion) {
        this.lwM2mVersion = lwM2mVersion;
    }

    public void setBindingMode(BindingMode bindingMode) {
        this.bindingMode = bindingMode;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        // TODO should probably better be done "implicitly" as part of the other setters
        this.lastUpdate = lastUpdate;
    }

    public void markLastRequestFailed() {
        this.failedLastRequest = true;
    }

    public boolean isAlive() {
        return this.failedLastRequest ? false : this.lastUpdate.getTime() + this.lifeTimeInSec * 1000 > System
                .currentTimeMillis();
    }

    @Override
    public String toString() {
        return String
                .format("Client [registrationDate=%s, address=%s, port=%s, lifeTimeInSec=%s, smsNumber=%s, lwM2mVersion=%s, bindingMode=%s, endpoint=%s, registrationId=%s, objectLinks=%s, lastUpdate=%s, failedLastRequest=%s]",
                        this.registrationDate, this.address, this.port, this.lifeTimeInSec, this.smsNumber, this.lwM2mVersion, this.bindingMode, this.endpoint,
                        this.registrationId, Arrays.toString(this.objectLinks), this.lastUpdate, this.failedLastRequest);
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
     * @return <code>true</code> if the other object is a Client instance and
     *         its <em>endpoint</em> property has the same value as this Client
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
