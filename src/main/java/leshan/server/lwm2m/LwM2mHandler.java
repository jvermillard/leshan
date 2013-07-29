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
package leshan.server.lwm2m;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.message.client.ClientMessage;
import leshan.server.lwm2m.message.client.MessageProcessor;
import leshan.server.lwm2m.session.Session;

import org.apache.mina.api.AbstractIoHandler;
import org.apache.mina.api.IoSession;

/**
 * Protocol logic for handling LW-M2M protocol.
 */
public class LwM2mHandler extends AbstractIoHandler {

    private final MessageProcessor processor = new LwM2mProcessor();

    @Override
    public void messageReceived(IoSession session, Object message) {

        if (message instanceof ClientMessage) {
            System.out.println("rcvd LW-M2M msg : " + message + " from " + session);

            LwM2mMessage response = ((ClientMessage) message).process(processor, new Session(session));
            session.write(response);

        } else {
            System.err.println("a LW-M2M message is expected");
        }
    }
}
