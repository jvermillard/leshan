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
public class Client {

    private static final long DEFAULT_LIFETIME_IN_SEC = 86400L;

    private static final String DEFAULT_LWM2M_VERSION = "1.0";

    private final Date registrationDate;

    private InetAddress address;

    private int port;

    private long lifeTimeInSec;

    private String smsNumber;

    private final String lwM2mVersion;

    private BindingMode bindingMode;

    private final String endpoint;

    private final String registrationId;

    private String[] objectLinks;

    public Client(String registrationId, String endpoint, InetAddress address, Integer port) {
        this(registrationId, endpoint, address, port, null, null, null, null, null);
    }

    public Client(String registrationId, String endpoint, InetAddress address, Integer port, String lwM2mVersion,
            Long lifetime, String smsNumber, BindingMode binding, String[] objectLinks) {
        this(registrationId, endpoint, address, port, lwM2mVersion, lifetime, smsNumber, binding, objectLinks, null);
    }

    public Client(String registrationId, String endpoint, InetAddress address, Integer port, String lwM2mVersion,
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

    /**
     * Updates a client's registration.
     * 
     * @param address the client's IP address
     * @param port the port the client's CoAP endpoint is listening on
     * @param lifetime the client's lifetime in seconds or <code>null</code> if the lifetime has not changed
     * since the client's last update of the registration
     * @param smsNumber the client's SMS number or <code>null</code> if the number has not changed since the
     * client's last update of the registration
     * @param binding the binding mode currently supported by the client as defined in section 5.2.1.1 of the LWM2M spec
     * or <code>null</code> if the supported mode has not changed since the client's last update of the registration
     * @param objectLinks the object types and instances supported by the client or <code>null</code> if the types and
     * instances have not changed since the client's last update of the registration
     * @throws IllegalArgumentException if the given binding mode is invalid
     */
    public void update(InetAddress address, Integer port, Long lifetime, String smsNumber, BindingMode bindingMode, String[] objectLinks) {
    	if (address != null) {
    		this.address = address;
    	}
    	if (port != null) {
    		this.port = port;
    	}
    	if (lifetime != null) {
        	this.lifeTimeInSec = lifetime;
    	}
    	if (smsNumber != null) {
    		this.smsNumber = smsNumber;
    	}
    	if (bindingMode != null) {
    		this.bindingMode = bindingMode;
    	}
    	if (objectLinks != null) {
    		this.objectLinks = objectLinks;
    	}
    }
    
    @Override
    public String toString() {
        return "Client [registrationDate=" + registrationDate + ", address=" + address + ", port=" + port
                + ", lifeTimeInSec=" + lifeTimeInSec + ", smsNumber=" + smsNumber + ", lwM2mVersion=" + lwM2mVersion
                + ", bindingMode=" + bindingMode + ", endpoint=" + endpoint + ", registrationId=" + registrationId
                + ", objectLinks=" + Arrays.toString(objectLinks) + "]";
    }

}
