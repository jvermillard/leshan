/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Zebra Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.resource;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.leshan.ObserveSpec;
import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ValueResponse;

public abstract class LwM2mClientNode {

    private static final long SECONDS_TO_MILLIS = 1000;

    private ScheduledExecutorService service;
    protected ObserveSpec observeSpec;
    private LwM2mNode previousValue;
    private Date previousTime;
    private NotifySender notifySender;

    public LwM2mClientNode() {
        this.observeSpec = new ObserveSpec.Builder().build();
    }

    public abstract ValueResponse read();

    public void observe(final NotifySender sender, final ScheduledExecutorService service) {
        this.notifySender = sender;
        this.service = service;
        updatePrevious(null);
        scheduleNext();
    }

    public LwM2mResponse write(LwM2mNode node) {
        return new LwM2mResponse(ResponseCode.METHOD_NOT_ALLOWED);
    }

    public LwM2mResponse writeAttributes(ObserveSpec spec) {
        this.observeSpec = spec;
        return new LwM2mResponse(ResponseCode.CHANGED);
    }

    public void valueChanged(LwM2mNode newValue) {
        if (shouldNotify(newValue)) {
            notifySender.sendNotify();
            updatePrevious(newValue);
        }
        scheduleNext();
    }

    private void updatePrevious(LwM2mNode node) {
        previousValue = node;
        previousTime = new Date();
    }

    private boolean shouldNotify(LwM2mNode node) {
        if (service == null || notifySender == null)
            return false;

        final long diff = getTimeDiff();
        final Integer pmax = observeSpec.getMaxPeriod();
        if (pmax != null && diff > pmax * SECONDS_TO_MILLIS) {
            return true;
        }
        return node != null && !node.equals(previousValue);
    }

    private void scheduleNext() {
        if (observeSpec.getMaxPeriod() != null) {
            long diff = getTimeDiff();
            service.schedule(new Runnable() {

                @Override
                public void run() {
                    notifySender.sendNotify();
                }
            }, observeSpec.getMaxPeriod() * SECONDS_TO_MILLIS - diff, TimeUnit.MILLISECONDS);
        }
    }

    private long getTimeDiff() {
        return new Date().getTime() - previousTime.getTime();
    }
}
