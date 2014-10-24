/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
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
package leshan.client.exchange;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import leshan.ObserveSpec;
import leshan.client.resource.LwM2mClientNode;
import leshan.client.response.LwM2mResponse;
import leshan.client.response.ObserveResponse;

public class ObserveNotifyExchange extends ForwardingLwM2mExchange implements Runnable {

    private static final long SECONDS_TO_MILLIS = 1000;

    private ObserveSpec observeSpec;

    private ScheduledExecutorService service;
    private LwM2mClientNode node;
    private byte[] previousValue;
    private Date previousTime;

    public ObserveNotifyExchange(final LwM2mExchange exchange, LwM2mClientNode node, ObserveSpec observeSpec,
            ScheduledExecutorService service) {
        super(exchange);
        this.node = node;
        this.observeSpec = observeSpec;
        this.service = service;
        updatePrevious(null);
        scheduleNext();
    }

    @Override
    public void respond(final LwM2mResponse response) {
        if (shouldNotify(response)) {
            sendNotify(response);
        }
        scheduleNext();
    }

    private void updatePrevious(byte[] responsePayload) {
        previousValue = responsePayload;
        previousTime = new Date();
    }

    private boolean shouldNotify(final LwM2mResponse response) {
        final long diff = getTimeDiff();
        final Integer pmax = observeSpec.getMaxPeriod();
        if (pmax != null && diff > pmax * SECONDS_TO_MILLIS) {
            return true;
        }
        return !Arrays.equals(response.getResponsePayload(), previousValue);
    }

    private void sendNotify(final LwM2mResponse response) {
        updatePrevious(response.getResponsePayload());
        exchange.respond(ObserveResponse.notifyWithContent(response.getResponsePayload()));
    }

    public void setObserveSpec(final ObserveSpec observeSpec) {
        this.observeSpec = observeSpec;
    }

    private void scheduleNext() {
        if (observeSpec.getMaxPeriod() != null) {
            long diff = getTimeDiff();
            service.schedule(this, observeSpec.getMaxPeriod() * SECONDS_TO_MILLIS - diff, TimeUnit.MILLISECONDS);
        }
    }

    private long getTimeDiff() {
        return new Date().getTime() - previousTime.getTime();
    }

    @Override
    public void run() {
        node.read(this);
    }

}
