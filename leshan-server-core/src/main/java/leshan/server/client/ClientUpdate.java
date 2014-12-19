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
package leshan.server.client;

import java.util.Arrays;
import java.util.Date;

import leshan.server.request.UpdateRequest;
import leshan.util.Validate;

/**
 * A container object for updating a LW-M2M client's registration properties on the server.
 * 
 * According to the LWM2M spec only those properties need to be set that have changed and thus should be updated on the
 * server.
 */
public class ClientUpdate {

    private UpdateRequest updateRequest = null;

    public ClientUpdate(UpdateRequest updateRequest) {
        this.updateRequest = updateRequest;
    }

    /**
     * Applies the registration property changes to a given client registration object.
     * 
     * @param client the registration to apply the changes to
     */
    public void apply(Client client) {
        Validate.notNull(client);

        if (updateRequest.getAddress() != null) {
            client.setAddress(updateRequest.getAddress());
        }

        if (updateRequest.getPort() != null) {
            client.setPort(updateRequest.getPort());
        }

        if (updateRequest.getObjectLinks() != null) {
            client.setObjectLinks(updateRequest.getObjectLinks());
        }

        if (updateRequest.getLifeTimeInSec() != null) {
            client.setLifeTimeInSec(updateRequest.getLifeTimeInSec());
        }

        if (updateRequest.getBindingMode() != null) {
            client.setBindingMode(updateRequest.getBindingMode());
        }

        if (updateRequest.getSmsNumber() != null) {
            client.setSmsNumber(updateRequest.getSmsNumber());
        }

        // this needs to be done in any case, even if no properties have changed, in order
        // to extend the client registration's time-to-live period ...
        client.setLastUpdate(new Date());
    }

    @Override
    public String toString() {
        return String
                .format("ClientUpdate [address=%s, port=%s, lifeTimeInSec=%s, smsNumber=%s, bindingMode=%s, registrationId=%s, objectLinks=%s]",
                        updateRequest.getAddress(), updateRequest.getPort(), updateRequest.getLifeTimeInSec(),
                        updateRequest.getSmsNumber(), updateRequest.getBindingMode(),
                        updateRequest.getRegistrationId(), Arrays.toString(updateRequest.getObjectLinks()));
    }

    public String getRegistrationId() {
        return updateRequest.getRegistrationId();
    }

}
