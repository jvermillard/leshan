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
package leshan.client.exchange.aggregate;

import leshan.ObserveSpec;
import leshan.client.exchange.LwM2mExchange;
import leshan.client.response.LwM2mResponse;

public class AggregatedLwM2mExchange implements LwM2mExchange {

    private final LwM2mResponseAggregator aggr;
    private final int id;
    private byte[] payload;

    public AggregatedLwM2mExchange(final LwM2mResponseAggregator aggr, final int id) {
        this.aggr = aggr;
        this.id = id;
    }

    @Override
    public void respond(final LwM2mResponse response) {
        aggr.respond(id, response);
    }

    @Override
    public byte[] getRequestPayload() {
        return payload;
    }

    public void setRequestPayload(final byte[] newPayload) {
        payload = newPayload;
    }

    @Override
    public boolean hasObjectInstanceId() {
        return false;
    }

    @Override
    public int getObjectInstanceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isObserve() {
        return aggr.getUnderlyingExchange().isObserve();
    }

    @Override
    public ObserveSpec getObserveSpec() {
        return aggr.getUnderlyingExchange().getObserveSpec();
    }

}
