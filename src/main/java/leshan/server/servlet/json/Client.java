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
package leshan.server.servlet.json;

import java.util.Date;

/**
 * JSON bean for a client.
 */
public class Client {

    private String endpoint;

    private String registrationId;

    private Date registrationDate;

    private String address;

    private String[] objects;

    private String smsNumber;

    private String lwM2mVersion;

    private long lifetime;

    public Client(String endpoint, String registrationId, Date registrationDate, String address, String[] objects,
            String smsNumber, String lwM2mVersion, long lifetime) {
        this.endpoint = endpoint;
        this.registrationId = registrationId;
        this.registrationDate = registrationDate;
        this.address = address;
        this.objects = objects;
        this.smsNumber = smsNumber;
        this.lwM2mVersion = lwM2mVersion;
        this.lifetime = lifetime;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String[] getObjects() {
        return objects;
    }

    public void setObjects(String[] objects) {
        this.objects = objects;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public String getLwM2mVersion() {
        return lwM2mVersion;
    }

    public void setLwM2mVersion(String lwM2mVersion) {
        this.lwM2mVersion = lwM2mVersion;
    }

    public long getLifetime() {
        return lifetime;
    }

    public void setLifetime(long lifetime) {
        this.lifetime = lifetime;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

}