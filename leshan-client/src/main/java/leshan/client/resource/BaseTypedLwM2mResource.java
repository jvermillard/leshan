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
package leshan.client.resource;

import leshan.client.exchange.LwM2mExchange;
import leshan.client.response.ExecuteResponse;
import leshan.client.response.ReadResponse;
import leshan.client.response.WriteResponse;

public abstract class BaseTypedLwM2mResource<E extends TypedLwM2mExchange<?>> extends LwM2mClientResource {

    protected abstract E createSpecificExchange(final LwM2mExchange exchange);

    @Override
    public final void read(final LwM2mExchange exchange) {
        handleRead(createSpecificExchange(exchange));
    }

    protected void handleRead(final E exchange) {
        exchange.advanced().respond(ReadResponse.notAllowed());
    }

    @Override
    public final void write(final LwM2mExchange exchange) {
        try {
            handleWrite(createSpecificExchange(exchange));
        } catch (final Exception e) {
            exchange.respond(WriteResponse.badRequest());
        }
    }

    protected void handleWrite(final E exchange) {
        exchange.advanced().respond(WriteResponse.notAllowed());
    }

    @Override
    public void execute(final LwM2mExchange exchange) {
        handleExecute(exchange);
    }

    protected void handleExecute(final LwM2mExchange exchange) {
        exchange.respond(ExecuteResponse.notAllowed());
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public final void notifyResourceUpdated() {
        if (observer != null) {
            observer.setObserveSpec(observeSpec);
            read(observer);
        }
    }

}
