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
package leshan.server.lwm2m.session;

import org.apache.mina.api.IoSession;

/**
 * A session to store some attributes of the connection between a LW-M2M client and the server.
 */
public class LwSession {

    private final IoSession ioSession;
    private RegistrationState registrationState;
    private String registrationId;
    private String endpoint;
    private long lifeTimeInSec;
    
    public LwSession(IoSession ioSession) {
        this.ioSession = ioSession;
    
    }
    public enum RegistrationState {
        UNREGISTERED, REGISTERED
    }
    public RegistrationState getRegistrationState() {
        return registrationState;
    }
    public void setRegistrationState(RegistrationState registrationState) {
        this.registrationState = registrationState;
    }
    public String getRegistrationId() {
        return registrationId;
    }
    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }
    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    public IoSession getIoSession() {
        return ioSession;
    }
    
    public long getLifeTimeInSec() {
        return lifeTimeInSec;
    }
    public void setLifeTimeInSec(long lifeTimeInSec) {
        this.lifeTimeInSec = lifeTimeInSec;
    }
}
