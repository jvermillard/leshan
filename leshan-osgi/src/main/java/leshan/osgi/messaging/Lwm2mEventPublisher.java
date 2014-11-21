/*
 * Copyright (c) 2014, Bosch Software Innovations GmbH
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
package leshan.osgi.messaging;

import java.util.Dictionary;
import java.util.Hashtable;

import leshan.core.node.LwM2mNode;
import leshan.core.node.LwM2mPath;
import leshan.osgi.Property;
import leshan.server.client.Client;
import leshan.server.observation.Observation;
import leshan.server.observation.ObservationListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes observed resources as events using OSGi Event Admin service. The
 * Lwm2mEventPublisher implements the {@link ObservationListener} to receive the
 * new Value from the observed resources.
 *
 */
public class Lwm2mEventPublisher implements ObservationListener {

    private static final Logger LOG = LoggerFactory.getLogger(Lwm2mEventPublisher.class);
    private final BundleContext ctx;

    /**
     * Constructor for new Lwm2mEventPublisher. The Lwm2mEventPublisher
     * implements the {@link ObservationListener} to receive the new Value from
     * the observed resources.
     *
     * @param bundleContext
     */
    public Lwm2mEventPublisher(final BundleContext bundleContext) {
        ctx = bundleContext;
    }

    @Override
    public void newValue(final Observation observation, final LwM2mNode node) {

        final Dictionary<String, Object> properties = new Hashtable<String, Object>();

        if (node == null || observation == null) {
            throw new IllegalArgumentException("Missing required propertie");
        } else {

            properties.put(Property.LWM2MNODE, node);
            properties.put(Property.LWM2MPATH, observation.getPath());
            properties.put(Property.CLIENT, observation.getClient());

            sendEvent(observation.getClient(), observation.getPath(), properties);
            LOG.trace("Received new value for observation from: {}", observation.getClient().getEndpoint());
        }
    }

    @Override
    public void cancelled(final Observation observation) {
        if (observation != null) {
            observation.cancel();
        }
    }

    private void sendEvent(final Client client, final LwM2mPath path, final Dictionary<String, Object> properties) {

        final ServiceReference<?> ref = ctx.getServiceReference(EventAdmin.class.getName());
        if (ref != null) {

            final StringBuilder topic = new StringBuilder();
            topic.append(client.getEndpoint());
            topic.append('/').append(path.getObjectId());
            if (path.getObjectInstanceId() != null) {
                topic.append('/').append(path.getObjectInstanceId());
                if (path.getResourceId() != null) {
                    topic.append('/').append(path.getResourceId());
                }
            }
            final Event notifyEvent = new Event(topic.toString(), properties);

            // postEvent sends events asynchronously -> Method does not block
            final EventAdmin eventAdmin = (EventAdmin) ctx.getService(ref);
            eventAdmin.postEvent(notifyEvent);
            LOG.trace("Sending event to topic {}", topic.toString());
        }
    }
}
