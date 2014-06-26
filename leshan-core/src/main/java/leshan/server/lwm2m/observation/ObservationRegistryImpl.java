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
package leshan.server.lwm2m.observation;

import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>Map</code> based registry for keeping track of this server's observed resources on LWM2M Clients.
 * 
 */
public class ObservationRegistryImpl implements ObservationRegistry {

    private final Logger LOG = LoggerFactory.getLogger(ObservationRegistryImpl.class);
    private final Map<String /* registration id */, Map<String /* resource path */, Observation>> observationsByClientAndResource;

    public ObservationRegistryImpl() {
        this.observationsByClientAndResource = new HashMap<String, Map<String, Observation>>();
    }

    @Override
    public synchronized void addObservation(Observation observation) {

        if (observation != null) {
            String registrationID = observation.getResourceProvider().getRegistrationId();

            Map<String, Observation> clientObservations = observationsByClientAndResource.get(registrationID);
            if (clientObservations == null) {
                clientObservations = new HashMap<String, Observation>();
                observationsByClientAndResource.put(registrationID, clientObservations);
            }

            Observation oldObservation = clientObservations.get(observation.getResourceRelativePath());
            if (oldObservation != null) {
                oldObservation.cancel();
            }
            clientObservations.put(observation.getResourceRelativePath(), observation);
        }
    }

    @Override
    public synchronized int cancelObservations(Client client) {
        int count = 0;
        if (client != null) {
            Map<String, Observation> clientObservations = observationsByClientAndResource.get(client
                    .getRegistrationId());

            if (clientObservations != null) {
                count = clientObservations.size();
                if (this.LOG.isTraceEnabled()) {
                    this.LOG.trace("Canceling {} observations of client {}", count, client.getEndpoint());
                }
                for (Observation obs : clientObservations.values()) {
                    obs.cancel();
                }
                clientObservations.clear();
                observationsByClientAndResource.remove(client.getEndpoint());
            }
        }
        return count;
    }

    @Override
    public synchronized void cancelObservation(Client client, String resourcepath) {
        if (client != null && resourcepath != null) {
            Map<String, Observation> clientObservations = observationsByClientAndResource.get(client
                    .getRegistrationId());

            if (clientObservations != null) {
                Observation observation = clientObservations.get(resourcepath);
                if (observation != null) {
                    if (this.LOG.isTraceEnabled()) {
                        this.LOG.trace("Canceling {} observation of client {}", resourcepath, client.getEndpoint());
                    }
                    observation.cancel();
                    clientObservations.remove(resourcepath);
                    if (clientObservations.isEmpty()) {
                        observationsByClientAndResource.remove(client.getEndpoint());
                    }
                }
            }
        }
    }
}
