/*
 * Copyright (c) 2014, Bosch Software Innovations GmbH
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
package leshan.osgi;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import leshan.LinkObject;
import leshan.core.response.ClientResponse;
import leshan.core.response.DiscoverResponse;
import leshan.core.response.ValueResponse;
import leshan.server.client.Client;
import leshan.server.request.DiscoverRequest;
import leshan.server.request.ExecuteRequest;
import leshan.server.request.LwM2mRequestSender;
import leshan.server.request.ObserveRequest;
import leshan.server.request.ReadRequest;
import leshan.server.request.WriteAttributesRequest;
import leshan.server.request.WriteRequest;

import org.osgi.service.device.Constants;

/**
 * The OsgiClientDevice implements the {@link OsgiClient} and contains the
 * properties for registration at OSGi Service Registry.
 */
public class OsgiClientDevice implements OsgiClient {

    private final Client client;

    private final LwM2mRequestSender requestSender;

    /**
     * Constructor for new LWM2MClientDevice.
     *
     * @param client {@link Client} the client.
     * @param requestSender {@link LwM2mRequestSender}
     */
    public OsgiClientDevice(final Client client, final LwM2mRequestSender requestSender) {
        this.client = client;
        this.requestSender = requestSender;
    }

    @Override
    public ValueResponse read(final ReadRequest readRequest) throws InterruptedException, UnsupportedEncodingException {

        return requestSender.send(readRequest);
    }

    @Override
    public ClientResponse write(final WriteRequest writeRequest) {

        return requestSender.send(writeRequest);
    }

    @Override
    public ClientResponse writeAttribute(final WriteAttributesRequest writeRequest) {

        return requestSender.send(writeRequest);
    }

    @Override
    public ClientResponse execute(final ExecuteRequest executeReqest) {

        return requestSender.send(executeReqest);
    }

    @Override
    public ValueResponse observe(final ObserveRequest observeRequest) {
        return requestSender.send(observeRequest);
    }

    @Override
    public DiscoverResponse discover(final DiscoverRequest discoverRequest) {

        return requestSender.send(discoverRequest);
    }

    @Override
    public LinkObject[] getObjectLinks() {
        return client.getObjectLinks();
    }

    @Override
    public Client getClient() {
        return client;
    }

    /**
     * Returns the ServiceProperties for OSGi Service Registry for the given
     * Client.
     *
     * @param client
     * @return Dictionary
     */
    public Dictionary<String, Object> getServiceRegistrationProperties(final Client client) {

        final Hashtable<String, Object> registrationProperties = new Hashtable<String, Object>();
        final Long expirationTime = new Date().getTime() + client.getLifeTimeInSec() * 1000;
        registrationProperties.put(Property.REGISTRATION_ID, client.getRegistrationId());
        registrationProperties.put(Constants.DEVICE_CATEGORY, new String[] { Property.CATEGORY_LWM2M_CLIENT });
        registrationProperties.put(Property.REGISTRATION_EXPIRATION, Long.toString(expirationTime));
        registrationProperties.put(Property.LWM2M_OBJECTS, client.getObjectLinks());
        registrationProperties.put(org.osgi.framework.Constants.SERVICE_PID, client.getEndpoint());

        return registrationProperties;
    }

    /**
     * Method specifies whether the client has reported in its specified
     * lifetime. There is a tolerance in the calculation of 10% of the lifetime.
     *
     * @return
     */
    public boolean isAlive() {

        return getClient().isMarkLastRequestTimedout() ? false
                : getClient().getLastUpdate().getTime() + (getClient().getLifeTimeInSec() * 1000)
                        + getIsAliveTolerance(getClient().getLifeTimeInSec()) > System.currentTimeMillis();
    }

    private long getIsAliveTolerance(final long lifeTimeInSec) {

        final long toleranceMax = 90000;
        final long toleranceMin = 30000;
        final long ltsec = lifeTimeInSec * 1000;
        final long t = ltsec / 100 * 10;
        if (t > toleranceMax) {
            return toleranceMax;
        } else if (t < toleranceMin) {
            return toleranceMin;
        }
        return t;
    }

}
