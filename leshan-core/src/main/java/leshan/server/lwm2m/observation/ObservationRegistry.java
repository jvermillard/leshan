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

import leshan.server.lwm2m.client.Client;

/**
 * A registry for keeping track of observed resources implemented by LWM2M
 * Clients.
 * 
 */
public interface ObservationRegistry {

    /**
     * Adds an observation of resource(s) to the registry.
     * 
     * @param observation the observation
     */
    void addObservation(Observation observation);

    /**
     * Cancels an observation of resource(s).
     * 
     * As a consequence the LWM2M Client will stop sending notifications about
     * updated values of resources in scope of the canceled observation.
     * 
     * @param observationId the ID of the observation to cancel (see
     *            {@link ResourceObserver#notify(byte[], int, String)}
     */
    void cancelObservation(String observationId);

    /**
     * Cancels all active observations of resource(s) implemented by a
     * particular LWM2M Client.
     * 
     * As a consequence the LWM2M Client will stop sending notifications about
     * updated values of resources in scope of the canceled observation.
     * 
     * @param client the LWM2M Client to cancel observations for
     */
    void cancelObservations(Client client);
}
