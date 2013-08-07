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
package leshan.server.lwm2m;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.client.DeregisterRequest;
import leshan.server.lwm2m.message.client.RequestProcessor;
import leshan.server.lwm2m.message.client.RegisterRequest;
import leshan.server.lwm2m.message.server.DeletedResponse;
import leshan.server.lwm2m.message.server.ErrorResponse;
import leshan.server.lwm2m.message.server.RegisterResponse;
import leshan.server.lwm2m.session.LwSession;
import leshan.server.lwm2m.session.LwSession.RegistrationState;
import leshan.server.lwm2m.session.SessionRegistry;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * React to received LWM2M message following the LWM2M protocol.
 */
public class LwM2mProcessor implements RequestProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mProcessor.class);

    private final SessionRegistry registry;

    public LwM2mProcessor(SessionRegistry registry) {
        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LwM2mMessage process(RegisterRequest message, LwSession session) {
        LOG.debug("processing a register message : " + message);

        if (session.getRegistrationState() != null) {
            LOG.debug("already registered");
            // the client should not be already registered
            return new ErrorResponse(message.getId(), ResponseCode.CONFLICT);
        }

        session.setRegistrationState(RegistrationState.REGISTERED);

        String registrationId = createRegistrationId();
        session.setRegistrationId(registrationId);
        session.setEndpoint(message.getEndpoint());
        session.setLwM2mVersion(message.getLwM2mVersion());
        session.setLifeTimeInSec(message.getLifetime());
        session.setObjects(message.getObjects());
        session.setSmsNumber(message.getSmsNumber());

        registry.add(session);
        return new RegisterResponse(message.getId(), registrationId);
    }

    private String createRegistrationId() {
        return RandomStringUtils.random(10, true, true);
    }

    @Override
    public LwM2mMessage process(DeregisterRequest message, LwSession session) {
        LOG.debug("processing a deregister message : " + message);

        // check registration location
        if (!message.getRegistrationId().equals(session.getRegistrationId())) {
            LOG.error("invalid registration id, expected '{}', was '{}'", session.getRegistrationId(),
                    message.getRegistrationId());
            return new ErrorResponse(message.getId(), ResponseCode.BAD_REQUEST); // location not found
        }

        // check state
        if (!RegistrationState.REGISTERED.equals(session.getRegistrationState())) {
            LOG.error("invalid session state, expected 'REGISTERED', was '{}'", session.getRegistrationState());
            return new ErrorResponse(message.getId(), ResponseCode.BAD_REQUEST);
        }

        session.setRegistrationState(RegistrationState.UNREGISTERED);
        session.getIoSession().close(false);

        return new DeletedResponse(message.getId());
    }

    @Override
    public void sessionClosed(LwSession session) {
        registry.remove(session);
    }
}
