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
package leshan.server.lwm2m.message.californium;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ObservationRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>Map</code> based registry for keeping track of this server's
 * observed resources on LWM2M Clients.
 * 
 */
final class InMemoryObservationRegistry implements ObservationRegistry {

    private final Logger LOG = LoggerFactory.getLogger(InMemoryObservationRegistry.class);
    private final Map<String, Observation> observationsById;
    private final Map<String, Set<Observation>> observationsByClient;

    public InMemoryObservationRegistry() {
        this.observationsById = new HashMap<String, Observation>();
        this.observationsByClient = new HashMap<String, Set<Observation>>();
    }

    @Override
    public synchronized void addObservation(Observation observation) {
        if (observation != null) {
            String endpoint = observation.getResourceProvider().getEndpoint();
            this.observationsById.put(observation.getId(), observation);
            Set<Observation> clientObservations = this.observationsByClient.get(endpoint);
            if (clientObservations == null) {
                clientObservations = new HashSet<Observation>();
                this.observationsByClient.put(endpoint, clientObservations);
            }
            clientObservations.add(observation);
        }
    }

    @Override
    public synchronized void cancelObservation(String observationId) {
        if (observationId == null) {
            return;
        }

        Observation observation = this.observationsById.get(observationId);
        if (observation != null) {
            if (this.LOG.isTraceEnabled()) {
                this.LOG.trace("Canceling {}", observation);
            }

            observation.cancel();
            this.observationsById.remove(observationId);
        }

        Set<Observation> set = this.observationsByClient.get(observation.getResourceProvider().getEndpoint());
        set.remove(observation);
    }

    @Override
    public synchronized void cancelObservations(Client client) {
        if (client == null) {
            return;
        }

        if (this.LOG.isTraceEnabled()) {
            this.LOG.trace("Canceling all observations of client {}", client.getEndpoint());
        }
        Set<Observation> clientObservations = this.observationsByClient.get(client.getEndpoint());
        if (clientObservations != null) {
            for (Observation obs : clientObservations) {
                obs.cancel();
            }
            clientObservations.clear();
            this.observationsByClient.remove(client.getEndpoint());
        }
    }

    public synchronized Observation getObservation(String observationId) {
        return this.observationsById.get(observationId);
    }
}