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
import java.util.Collection;
import java.util.Date;

/**
 * A LW-M2M client registered on the server
 */
public interface Client {

    String getEndpoint();

    String getRegistrationId();

    Date getRegistrationDate();

    InetAddress getAddress();

    int getPort();

    long getLifeTimeInSec();

    String getSmsNumber();

    String getLwM2mVersion();

    BindingMode getBindingMode();

    /**
     * @return the list of supported objects in the CoRE Link Format.
     */
    Collection<String> getObjectLinks();

    /**
     * Returns <code>true</code> if the given object (or object instance) is supported/available on the LWM2M client.
     * <p>
     * The list of supported objects and object instances is provided by the client during the registration phase.
     * </p>
     * 
     * @param objectId the identifier of the object/object instance
     * @return <code>true</code> if the object is available for this client and <code>false</code> otherwise.
     */
    boolean supportObject(String objectId);

}
