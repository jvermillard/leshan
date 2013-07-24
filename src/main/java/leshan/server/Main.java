/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package leshan.server;

import java.nio.ByteBuffer;

import org.apache.mina.api.IdleStatus;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.coap.codec.CoapDecoder;
import org.apache.mina.coap.codec.CoapEncoder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.query.RequestFilter;
import org.apache.mina.transport.bio.BioUdpServer;

public class Main {

    public static void main(String[] args) {
        // setup the UDP server
        
        // we use BIO because we have only one socket to listen
        BioUdpServer server = new BioUdpServer();
        
        // we kill sessions after 20 minutes of inactivity
        server.getSessionConfig().setIdleTimeInMillis(IdleStatus.READ_IDLE, 20 * 60 * 1_000);
        
        server.setFilters(new ProtocolCodecFilter<CoapMessage, ByteBuffer, Void, Void>(new CoapEncoder(), new CoapDecoder()),new RequestFilter<CoapMessage,CoapMessage>());
        
        // bind the IANA assigned UDP port for CoAP (so for LWM2M)
        server.bind(5683);
    }
}
