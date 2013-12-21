/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package leshan.server.lwm2m.client;

import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.lang.Validate;

/**
 * A LW-M2M client registered on the server
 */
public class Client {
    private static final long DEFAULT_LIFETIME_IN_SEC = 86400L;

    private static final String DEFAULT_LWM2M_VERSION = "1.0";

    private final Date registrationDate;

    private final InetAddress address;

    private final int port;

    private final long lifeTimeInSec;

    private final String smsNumber;

    private final String lwM2mVersion;

    private final BindingMode bindingMode;

    private final String endpoint;

    private final String registrationId;

    private final String[] objectLinks;

    public Client(String registrationId, String endpoint, InetAddress address, Integer port) {
        this(registrationId, endpoint, address, port, null, null, null, null, null);
    }

    public Client(String registrationId, String endpoint, InetAddress address, Integer port, String lwM2mVersion,
            Long lifetime, String smsNumber, BindingMode binding, String[] objectLinks) {

        Validate.notEmpty(endpoint);
        Validate.notNull(address);
        Validate.notNull(port);

        this.registrationId = registrationId;
        this.endpoint = endpoint;
        this.address = address;
        this.port = port;
        this.objectLinks = objectLinks;
        this.registrationDate = new Date();
        this.lifeTimeInSec = lifetime == null ? DEFAULT_LIFETIME_IN_SEC : lifetime;
        this.lwM2mVersion = lwM2mVersion == null ? DEFAULT_LWM2M_VERSION : lwM2mVersion;
        this.bindingMode = binding == null ? BindingMode.U : binding;
        this.smsNumber = smsNumber;
    }


    public String getRegistrationId() {
        return registrationId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String[] getObjectLinks() {
        return objectLinks;
    }

    public long getLifeTimeInSec() {
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

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientResource [getRegistrationId()=").append(registrationId).append(", getEndpoint()=")
                .append(endpoint).append(", registrationDate=").append(registrationDate).append(", address=")
                .append(address).append(", port=").append(port).append(", lifeTimeInSec=").append(lifeTimeInSec)
                .append(", smsNumber=").append(smsNumber).append(", lwM2mVersion=").append(lwM2mVersion)
                .append(", bindingMode=").append(bindingMode).append("]");
        return builder.toString();
    }

}
