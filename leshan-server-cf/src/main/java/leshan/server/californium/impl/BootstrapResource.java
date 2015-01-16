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
package leshan.server.californium.impl;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import leshan.server.bootstrap.BootstrapConfig;
import leshan.server.bootstrap.BootstrapConfig.ServerConfig;
import leshan.server.bootstrap.BootstrapConfig.ServerSecurity;
import leshan.server.bootstrap.BootstrapStore;
import leshan.tlv.Tlv;
import leshan.tlv.Tlv.TlvType;
import leshan.tlv.TlvEncoder;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.MessageObserver;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapResource extends CoapResource {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapResource.class);
    private static final String QUERY_PARAM_ENDPOINT = "ep=";

    private BootstrapStore store;

    private Executor e = Executors.newFixedThreadPool(5);

    public BootstrapResource(BootstrapStore store) {
        super("bs");
        this.store = store;
    }

    @Override
    public void handlePOST(final CoapExchange exchange) {
        Request request = exchange.advanced().getRequest();
        LOG.debug("POST received : {}", request);
        // The LW M2M spec (section 8.2) mandates the usage of Confirmable
        // messages
        if (!Type.CON.equals(request.getType())) {
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }

        // which endpoint?
        String endpointTmp = null;
        for (String param : request.getOptions().getUriQuery()) {
            if (param.startsWith(QUERY_PARAM_ENDPOINT)) {
                endpointTmp = param.substring(QUERY_PARAM_ENDPOINT.length());
                break;
            }
        }
        if (endpointTmp == null) {
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }
        final String endpoint = endpointTmp;

        // TODO check security of the endpoint

        final BootstrapConfig cfg = store.getBootstrap(endpoint);
        if (cfg == null) {
            LOG.error("No bootstrap config for {}", endpoint);
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }
        exchange.respond(ResponseCode.CHANGED);

        // now push the config

        e.execute(new Runnable() {

            @Override
            public void run() {
                // first delete everything

                final Endpoint e = exchange.advanced().getEndpoint();
                Request deleteAll = Request.newDelete();
                deleteAll.getOptions().addUriPath("/");
                deleteAll.setConfirmable(true);
                deleteAll.setDestination(exchange.getSourceAddress());
                deleteAll.setDestinationPort(exchange.getSourcePort());

                deleteAll.send(e).addMessageObserver(new MessageObserver() {

                    @Override
                    public void onTimeout() {
                        LOG.debug("Bootstrap delete {} timeout!", endpoint);
                    }

                    @Override
                    public void onRetransmission() {
                        LOG.debug("Bootstrap delete {} retransmission", endpoint);
                    }

                    @Override
                    public void onResponse(Response response) {
                        LOG.debug("Bootstrap delete {} return code {}", endpoint, response.getCode());
                        List<Integer> toSend = new ArrayList<>(cfg.security.keySet());
                        sendBootstrap(e, endpoint, exchange.getSourceAddress(), exchange.getSourcePort(), cfg, toSend);
                    }

                    @Override
                    public void onReject() {
                        LOG.debug("Bootstrap delete {} reject", endpoint);
                    }

                    @Override
                    public void onCancel() {
                        LOG.debug("Bootstrap delete {} cancel", endpoint);
                    }

                    @Override
                    public void onAcknowledgement() {
                        LOG.debug("Bootstrap delete {} acknowledgement", endpoint);
                    }
                });
            }
        });
    }

    private void sendBootstrap(final Endpoint e, final String endpoint, final InetAddress targetAddress,
            final int targetPort, final BootstrapConfig cfg, final List<Integer> toSend) {

        if (!toSend.isEmpty()) {
            // 1st encode them into a juicy TLV binary
            Integer key = toSend.remove(0);

            Tlv[] secuResources = tlvEncode(cfg.security.get(key));

            ByteBuffer encoded = TlvEncoder.encode(secuResources);
            // now send security
            Request postSecurity = Request.newPut();
            postSecurity.getOptions().addUriPath("0");
            postSecurity.getOptions().addUriPath(key.toString());
            postSecurity.setConfirmable(true);
            postSecurity.setDestination(targetAddress);
            postSecurity.setDestinationPort(targetPort);
            postSecurity.setPayload(encoded.array());

            postSecurity.send(e).addMessageObserver(new MessageObserver() {

                @Override
                public void onTimeout() {
                    LOG.debug("Bootstrap security {} timeout!", endpoint);
                }

                @Override
                public void onRetransmission() {
                    LOG.debug("Bootstrap security {} retransmission", endpoint);
                }

                @Override
                public void onResponse(Response response) {
                    LOG.debug("Bootstrap security {} return code {}", endpoint, response.getCode());
                    // recursive call until toSend is empty
                    sendBootstrap(e, endpoint, targetAddress, targetPort, cfg, toSend);
                }

                @Override
                public void onReject() {
                    LOG.debug("Bootstrap security {} reject", endpoint);
                }

                @Override
                public void onCancel() {
                    LOG.debug("Bootstrap security {} cancel", endpoint);
                }

                @Override
                public void onAcknowledgement() {
                    LOG.debug("Bootstrap security {} acknowledgement", endpoint);
                }
            });

        } else {
            // we are done, send the servers
            List<Integer> serversToSend = new ArrayList<>(cfg.servers.keySet());
            sendServers(e, endpoint, targetAddress, targetPort, cfg, serversToSend);
        }
    }

    private void sendServers(final Endpoint e, final String endpoint, final InetAddress targetAddress,
            final int targetPort, final BootstrapConfig cfg, final List<Integer> toSend) {

        if (!toSend.isEmpty()) {
            // 1st encode them into a juicy TLV binary
            Integer key = toSend.remove(0);

            Tlv[] serverResources = tlvEncode(cfg.servers.get(key));
            ByteBuffer encoded = TlvEncoder.encode(serverResources);

            // now send server
            Request postServer = Request.newPut();
            postServer.getOptions().addUriPath("1");
            postServer.getOptions().addUriPath(key.toString());
            postServer.setConfirmable(true);
            postServer.setDestination(targetAddress);
            postServer.setDestinationPort(targetPort);
            postServer.setPayload(encoded.array());
            postServer.send(e).addMessageObserver(new MessageObserver() {
                @Override
                public void onTimeout() {
                    LOG.debug("Bootstrap servers {} timeout!", e);
                }

                @Override
                public void onRetransmission() {
                    LOG.debug("Bootstrap servers {} retransmission", e);
                }

                @Override
                public void onResponse(Response response) {
                    LOG.debug("Bootstrap servers {} return code {}", e, response.getCode());
                    // recursive call until toSend is empty
                    sendServers(e, endpoint, targetAddress, targetPort, cfg, toSend);
                }

                @Override
                public void onReject() {
                    LOG.debug("Bootstrap servers {} reject", endpoint);
                }

                @Override
                public void onCancel() {
                    LOG.debug("Bootstrap servers {} cancel", endpoint);
                }

                @Override
                public void onAcknowledgement() {
                    LOG.debug("Bootstrap servers {} acknowledgement", endpoint);
                }
            });

        } else {
            // done
            LOG.debug("Bootstrap session done for endpoint {}", endpoint);
        }
    }

    private Tlv[] tlvEncode(ServerSecurity value) {
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
        return resources;
    }

    private Tlv[] tlvEncode(ServerConfig value) {
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

        return resources.toArray(new Tlv[] {});
    }
}
