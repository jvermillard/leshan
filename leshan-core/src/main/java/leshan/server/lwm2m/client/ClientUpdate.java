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
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.Validate;

/**
 * A LW-M2M client registered on the server
 */
public class ClientUpdate {

    private static final long DEFAULT_LIFETIME_IN_SEC = 86400L;

    private static final String DEFAULT_LWM2M_VERSION = "1.0";

    private InetAddress address;

    private int port;

    private long lifeTimeInSec;

    private String smsNumber;

    private String lwM2mVersion;

    private BindingMode bindingMode;

    private final String registrationId;

    private final String[] objectLinks;

    public ClientUpdate(String registrationId, InetAddress address, int port) {
        this(registrationId, address, port, null, null, null, null, null);
    }

    public ClientUpdate(String registrationId, InetAddress address, int port, String lwM2mVersion, Long lifetime,
            String smsNumber, BindingMode binding, String[] objectLinks) {
        this(registrationId, address, port, lwM2mVersion, lifetime, smsNumber, binding, objectLinks, null);
    }

    public ClientUpdate(String registrationId, InetAddress address, int port, String lwM2mVersion, Long lifetime,
            String smsNumber, BindingMode binding, String[] objectLinks, Date registrationDate) {

        Validate.notEmpty(registrationId);
        this.registrationId = registrationId;
        this.address = address;
        this.port = port;
        this.objectLinks = objectLinks;
        this.lifeTimeInSec = lifetime == null ? DEFAULT_LIFETIME_IN_SEC : lifetime;
        this.lwM2mVersion = lwM2mVersion == null ? DEFAULT_LWM2M_VERSION : lwM2mVersion;
        this.bindingMode = binding == null ? BindingMode.U : binding;
        this.smsNumber = smsNumber;
    }

    public String getRegistrationId() {
        return registrationId;
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

    @Override
    public String toString() {
        return "ClientUpdate [address=" + address + ", port=" + port + ", lifeTimeInSec=" + lifeTimeInSec
                + ", smsNumber=" + smsNumber + ", lwM2mVersion=" + lwM2mVersion + ", bindingMode=" + bindingMode
                + ", registrationId=" + registrationId + ", objectLinks=" + Arrays.toString(objectLinks) + "]";
    }
}