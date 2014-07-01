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

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResourceSpec;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ResourceObserver;
import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.MessageObserverAdapter;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

public final class CaliforniumBasedObservation extends MessageObserverAdapter implements Observation {

    private final Request coapRequest;
    private final ResourceObserver observer;
    private final ResourceSpec target;

    public CaliforniumBasedObservation(Request request, ResourceObserver observer, ResourceSpec target) {
        if (request == null) {
            throw new NullPointerException("CoAP Request must not be null");
        }
        if (observer == null) {
            throw new NullPointerException("Resource Observer must not be null");
        }
        if (target == null) {
            throw new NullPointerException("Target must not be null");
        }
        request.addMessageObserver(this);
        coapRequest = request;
        this.observer = observer;
        this.target = target;
    }

    @Override
    public void cancel() {
        coapRequest.cancel();
    }

    @Override
    public void onResponse(Response response) {
        if (response.getCode() == CoAP.ResponseCode.CHANGED) {
            ContentFormat format = ContentFormat.fromCode(response.getOptions().getContentFormat());
            if (format == null) {
                format = ContentFormat.TEXT;
            }
            observer.notify(response.getPayload(), format, target);
        }
    }

    @Override
    public Client getResourceProvider() {
        return target.getClient();
    }

    @Override
    public Integer getObjectId() {
        return target.getObjectId();
    }

    @Override
    public Integer getObjectInstanceId() {
        return target.getObjectInstanceId();
    }

    @Override
    public Integer getResourceId() {
        return target.getResourceId();
    }

    @Override
    public ResourceObserver getResourceObserver() {
        return observer;
    }

    @Override
    public String toString() {
        return String.format("CaliforniumObservation [%s]", target);
    }

    @Override
    public String getResourceRelativePath() {
        return target.asRelativePath();
    }
}
