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
package leshan.server.lwm2m.osgi;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.DiscoverRequest;
import leshan.server.lwm2m.request.DiscoverResponse;
import leshan.server.lwm2m.request.ExecuteRequest;
import leshan.server.lwm2m.request.LwM2mRequestSender;
import leshan.server.lwm2m.request.ObserveRequest;
import leshan.server.lwm2m.request.ReadRequest;
import leshan.server.lwm2m.request.ValueResponse;
import leshan.server.lwm2m.request.WriteAttributesRequest;
import leshan.server.lwm2m.request.WriteRequest;

import org.osgi.service.device.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LWM2MClientDevice implements LWM2MClient {

    private static final Logger LOG = LoggerFactory.getLogger(LWM2MClientDevice.class);

    private final Client client;

    private final LwM2mRequestSender requestSender;

    public LWM2MClientDevice(Client client, LwM2mRequestSender requestSender) {
        this.client = client;
        this.requestSender = requestSender;
    }

    @Override
    public ValueResponse read(ReadRequest readRequest) throws InterruptedException, UnsupportedEncodingException {

        final ValueResponse vrs = (ValueResponse) requestSender.send(readRequest);
        return vrs;
    }

    @Override
    public ClientResponse write(WriteRequest writeRequest) {

        return requestSender.send(writeRequest);
    }

    @Override
    public ClientResponse writeAttribute(WriteAttributesRequest writeRequest) {

        return requestSender.send(writeRequest);
    }

    @Override
    public ClientResponse execute(ExecuteRequest executeReqest) {

        return requestSender.send(executeReqest);
    }

    @Override
    public ValueResponse observe(ObserveRequest observeRequest) {
        ValueResponse vrs = (ValueResponse) requestSender.send(observeRequest);
        return vrs;
    }

    @Override
    public DiscoverResponse discover(DiscoverRequest discoverRequest) {

        DiscoverResponse drs = (DiscoverResponse) requestSender.send(discoverRequest);
        return drs;
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
     * Returns the ServiceProperties for OSGi Service Registry for the given Client.
     * 
     * @param client
     * @return Dictionary
     */
    public Dictionary<String, Object> getServiceRegistrationProperties(Client client) {

        Hashtable<String, Object> registrationProperties = new Hashtable<String, Object>();
        Long expirationTime = new Date().getTime() + client.getLifeTimeInSec() * 1000;
        registrationProperties.put(Property.REGISTRATION_ID, client.getRegistrationId());
        registrationProperties.put(Constants.DEVICE_CATEGORY, new String[] { Property.CATEGORY_LWM2M_CLIENT });
        registrationProperties.put(Property.REGISTRATION_EXPIRATION, Long.toString(expirationTime));
        registrationProperties.put(Property.LWM2M_OBJECTS, client.getObjectLinks());
        registrationProperties.put(org.osgi.framework.Constants.SERVICE_PID, client.getEndpoint());

        return registrationProperties;
    }

    /**
     * Method specifies whether the client has reported in its specified lifetime. There is a
     * tolerance in the calculation of 10% of the lifetime.
     * 
     * @return
     */
    public boolean isAlive() {

        return getClient().isMarkLastRequestTimedout() ? false : getClient().getLastUpdate().getTime()
                + (getClient().getLifeTimeInSec() * 1000) + getIsAliveTolerance(getClient().getLifeTimeInSec()) > System
                .currentTimeMillis();
    }

    private long getIsAliveTolerance(long lifeTimeInSec) {

        long toleranceMax = 90000;
        long toleranceMin = 30000;
        lifeTimeInSec = lifeTimeInSec * 1000;
        long t = lifeTimeInSec / 100 * 10;
        if (t > toleranceMax) {
            return toleranceMax;
        } else if (t < toleranceMin) {
            return toleranceMin;
        }
        return t;
    }

}
