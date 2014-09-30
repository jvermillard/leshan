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
package leshan.server.lwm2m.impl.californium;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import leshan.server.lwm2m.bootstrap.BootstrapConfig;
import leshan.server.lwm2m.bootstrap.BootstrapConfig.ServerConfig;
import leshan.server.lwm2m.bootstrap.BootstrapConfig.ServerSecurity;
import leshan.server.lwm2m.bootstrap.BootstrapStore;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.CoAP.Type;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Endpoint;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class BootstrapResource extends ResourceBase {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapResource.class);
    private static final String QUERY_PARAM_ENDPOINT = "ep=";
    private static final int TIMEOUT_MILLI = 20_000;

    private BootstrapStore store;

    public BootstrapResource(BootstrapStore store) {
        super("bs");
        this.store = store;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        Request request = exchange.advanced().getRequest();
        LOG.debug("POST received : {}", request);
        // The LW M2M spec (section 8.2) mandates the usage of Confirmable
        // messages
        if (!Type.CON.equals(request.getType())) {
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }

        // which endpoint?
        String endpoint = null;
        for (String param : request.getOptions().getURIQueries()) {
            if (param.startsWith(QUERY_PARAM_ENDPOINT)) {
                endpoint = param.substring(QUERY_PARAM_ENDPOINT.length());
                break;
            }
        }
        if (endpoint == null) {
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }

        // TODO check security of the endpoint

        BootstrapConfig cfg = store.getBootstrap(endpoint);
        if (cfg == null) {
            LOG.error("No bootstrap config for {}", endpoint);
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }
        exchange.respond(ResponseCode.CHANGED);

        // now push the config

        // first delete everything

        Endpoint e = exchange.advanced().getEndpoint();
        Request deleteAll = Request.newDelete();
        deleteAll.getOptions().addURIPath("/");
        deleteAll.setConfirmable(true);
        deleteAll.setDestination(exchange.getSourceAddress());
        deleteAll.setDestinationPort(exchange.getSourcePort());

        try {
            Response response = deleteAll.send(e).waitForResponse(TIMEOUT_MILLI);
            if (response == null) {
                LOG.error("Bootstrap {} delete timeout", endpoint);
                return;
            }
            LOG.debug("Bootstrap delete {} return code {}", endpoint, response.getCode());
        } catch (InterruptedException e1) {
            // get out!the server is stopping
            return;
        }
        // send security elements

        // 1st encode them into a juicy TLV binary
        Tlv[] secuInstances = new Tlv[cfg.security.size()];
        int idx = 0;
        for (Map.Entry<Integer, BootstrapConfig.ServerSecurity> entry : cfg.security.entrySet()) {
            // create the security entry for this server
            secuInstances[idx++] = tlvEncode(entry.getKey(), entry.getValue());
        }
        ByteBuffer encoded = TlvEncoder.encode(secuInstances);

        Request postSecurity = Request.newPost();
        postSecurity.getOptions().addURIPath("/0");
        postSecurity.setConfirmable(true);
        postSecurity.setDestination(exchange.getSourceAddress());
        postSecurity.setDestinationPort(exchange.getSourcePort());
        postSecurity.setPayload(encoded.array());

        try {
            Response response = postSecurity.send(e).waitForResponse(TIMEOUT_MILLI);
            if (response == null) {
                LOG.error("security bootstrap of {} timeout", endpoint);
                return;
            }
            LOG.debug("Security bootstrap of {} returned code {}", endpoint, response.getCode());
        } catch (InterruptedException e1) {
            // get out!the server is stopping
            return;
        }

        // send the server settings
        Tlv[] serverInstances = new Tlv[cfg.servers.size()];
        idx = 0;
        for (Map.Entry<Integer, BootstrapConfig.ServerConfig> entry : cfg.servers.entrySet()) {
            // create the security entry for this server
            serverInstances[idx++] = tlvEncode(entry.getKey(), entry.getValue());
        }
        encoded = TlvEncoder.encode(serverInstances);

        Request postServer = Request.newPost();
        postServer.getOptions().addURIPath("/1");
        postServer.setConfirmable(true);
        postServer.setDestination(exchange.getSourceAddress());
        postServer.setDestinationPort(exchange.getSourcePort());
        postServer.setPayload(encoded.array());

        try {
            Response response = postServer.send(e).waitForResponse(TIMEOUT_MILLI);
            if (response == null) {
                LOG.error("server list bootstrap of {} timeout", endpoint);
                return;
            }
            LOG.debug("Server list bootstrap of {} returned code {}", endpoint, response.getCode());
        } catch (InterruptedException e1) {
            // get out!the server is stopping
            return;
        }
    }

    private Tlv tlvEncode(int key, ServerSecurity value) {
        Tlv[] resources = new Tlv[12];
        resources[0] = new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeString(value.uri), 0);
        resources[1] = new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeBoolean(value.bootstrapServer), 1);
        resources[2] = new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.securityMode.code), 2);
        resources[3] = new Tlv(TlvType.RESOURCE_INSTANCE, null, value.publicKeyOrId, 3);
        resources[4] = new Tlv(TlvType.RESOURCE_INSTANCE, null, value.serverPublicKeyOrId, 4);
        resources[5] = new Tlv(TlvType.RESOURCE_INSTANCE, null, value.secretKey, 5);
        resources[6] = new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.smsSecurityMode.code), 6);
        resources[7] = new Tlv(TlvType.RESOURCE_INSTANCE, null, value.smsBindingKeyParam, 7);
        resources[8] = new Tlv(TlvType.RESOURCE_INSTANCE, null, value.smsBindingKeySecret, 8);
        resources[9] = new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeString(value.serverSmsNumber), 9);
        resources[10] = new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.serverId), 10);
        resources[11] = new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.clientOldOffTime), 11);
        return new Tlv(TlvType.OBJECT_INSTANCE, resources, null, key);
    }

    private Tlv tlvEncode(int key, ServerConfig value) {
        List<Tlv> resources = new ArrayList<Tlv>();
        resources.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.shortId), 0));
        resources.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.lifetime), 1));
        resources.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.defaultMinPeriod), 2));
        if (value.defaultMaxPeriod != null) {
            resources
                    .add(new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.defaultMaxPeriod), 3));
        }
        if (value.disableTimeout != null) {
            resources.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeInteger(value.disableTimeout), 5));
        }
        resources.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeBoolean(value.notifIfDisabled), 6));
        resources.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, TlvEncoder.encodeString(value.binding.name()), 7));

        return new Tlv(TlvType.OBJECT_INSTANCE, resources.toArray(new Tlv[] {}), null, key);
    }
}
