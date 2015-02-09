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
 *     Sierra Wireless, - initial API and implementation
 *     Bosch Software Innovations GmbH, - initial API and implementation
 *******************************************************************************/

package org.eclipse.leshan.client.example;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.resource.LwM2mClientObjectDefinition;
import org.eclipse.leshan.client.resource.LwM2mClientResource;
import org.eclipse.leshan.client.resource.ResourceDefinition;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.Value;
import org.eclipse.leshan.core.request.DeregisterRequest;
import org.eclipse.leshan.core.request.RegisterRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.RegisterResponse;
import org.eclipse.leshan.core.response.ValueResponse;

/*
 * To build: 
 * mvn assembly:assembly -DdescriptorId=jar-with-dependencies
 * To use:
 * java -jar target/leshan-client-*-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 5683 9000
 */
public class LeshanClientExample {
    private String registrationID;

    public static void main(final String[] args) {
        if (args.length < 4) {
            System.out
                    .println("Usage:\njava -jar target/leshan-client-example-*-SNAPSHOT-jar-with-dependencies.jar [Client IP] [Client port] [Server IP] [Server Port]");
        } else {
            new LeshanClientExample(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
        }
    }

    public LeshanClientExample(final String localHostName, final int localPort, final String serverHostName,
            final int serverPort) {
        final LwM2mClientObjectDefinition objectDevice = createObjectDefinition();
        final InetSocketAddress clientAddress = new InetSocketAddress(localHostName, localPort);
        final InetSocketAddress serverAddress = new InetSocketAddress(serverHostName, serverPort);

        final LeshanClient client = new LeshanClient(clientAddress, serverAddress, objectDevice);
        // Start the client
        client.start();

        // Register to the server provided
        final String endpointIdentifier = UUID.randomUUID().toString();
        final RegisterRequest registerRequest = new RegisterRequest(endpointIdentifier);
        RegisterResponse response = client.send(registerRequest);

        // Report registration response.
        System.out.println("Device Registration (Success? " + response.getCode() + ")");
        if (response.getCode() == ResponseCode.CREATED) {
            System.out.println("\tDevice: Registered Client Location '" + response.getRegistrationID() + "'");
            registrationID = response.getRegistrationID();
        } else {
            // TODO Should we have a error message on response ?
            // System.err.println("\tDevice Registration Error: " + response.getErrorMessage());
            System.err.println("\tDevice Registration Error: " + response.getCode());
            System.err
                    .println("If you're having issues connecting to the LWM2M endpoint, try using the DTLS port instead");
        }

        // Deregister on shutdown and stop client.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (registrationID != null) {
                    System.out.println("\tDevice: Deregistering Client '" + registrationID + "'");
                    final DeregisterRequest deregisterRequest = new DeregisterRequest(registrationID);
                    client.send(deregisterRequest);
                    client.stop();
                }
            }
        });
    }

    private LwM2mClientObjectDefinition createObjectDefinition() {
        // Create an object model
        final StringValueResource manufacturerResource = new StringValueResource("Leshan Example Device");
        final StringValueResource modelResource = new StringValueResource("Model 500");
        final StringValueResource serialNumberResource = new StringValueResource("LT-500-000-0001");
        final StringValueResource firmwareResource = new StringValueResource("1.0.0");
        final ExecutableResource rebootResource = new ExecutableResource();
        final ExecutableResource factoryResetResource = new ExecutableResource();
        final IntegerMultipleResource powerAvailablePowerResource = new IntegerMultipleResource(new Integer[] { 0, 4 });
        final IntegerMultipleResource powerSourceVoltageResource = new IntegerMultipleResource(new Integer[] { 12000,
                                5000 });
        final IntegerMultipleResource powerSourceCurrentResource = new IntegerMultipleResource(
                new Integer[] { 150, 75 });
        final IntegerValueResource batteryLevelResource = new IntegerValueResource(92);
        final MemoryFreeResource memoryFreeResource = new MemoryFreeResource();
        final IntegerMultipleResource errorCodeResource = new IntegerMultipleResource(new Integer[] { 0 });
        final TimeResource currentTimeResource = new TimeResource();
        final StringValueResource utcOffsetResource = new StringValueResource(new SimpleDateFormat("X").format(Calendar
                .getInstance().getTime()));
        final StringValueResource timezoneResource = new StringValueResource(TimeZone.getDefault().getID());
        final StringValueResource bindingsResource = new StringValueResource("U");

        final LwM2mClientObjectDefinition objectDevice = new LwM2mClientObjectDefinition(3, true, true,
                new ResourceDefinition(0, manufacturerResource, true), new ResourceDefinition(1, modelResource, true),
                new ResourceDefinition(2, serialNumberResource, true),
                new ResourceDefinition(3, firmwareResource, true), new ResourceDefinition(4, rebootResource, true),
                new ResourceDefinition(5, factoryResetResource, true), new ResourceDefinition(6,
                        powerAvailablePowerResource, true),
                new ResourceDefinition(7, powerSourceVoltageResource, true), new ResourceDefinition(8,
                        powerSourceCurrentResource, true), new ResourceDefinition(9, batteryLevelResource, true),
                new ResourceDefinition(10, memoryFreeResource, true), new ResourceDefinition(11, errorCodeResource,
                        true), new ResourceDefinition(12, new ExecutableResource(), true), new ResourceDefinition(13,
                        currentTimeResource, true), new ResourceDefinition(14, utcOffsetResource, true),
                new ResourceDefinition(15, timezoneResource, true), new ResourceDefinition(16, bindingsResource, true));
        return objectDevice;
    }

    public class TimeResource extends LwM2mClientResource {

        public TimeResource() {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    valueChanged(new LwM2mResource(getId(), Value.newDateValue(new Date())));
                }
            }, 5000, 5000);
        }

        @Override
        public ValueResponse read() {
            System.out.println("\tDevice: Reading Current Device Time.");
            return new ValueResponse(ResponseCode.CONTENT, new LwM2mResource(getId(), Value.newDateValue(new Date())));
        }
    }

    public class MemoryFreeResource extends LwM2mClientResource {
        @Override
        public ValueResponse read() {
            System.out.println("\tDevice: Reading Memory Free Resource");
            final Random rand = new Random();
            return new ValueResponse(ResponseCode.CONTENT, new LwM2mResource(getId(), Value.newIntegerValue(114 + rand
                    .nextInt(50))));
        }
    }

    private class IntegerMultipleResource extends LwM2mClientResource {

        private final Integer[] values;

        public IntegerMultipleResource(final Integer[] values) {
            this.values = values;
        }

        @Override
        public ValueResponse read() {
            Value<?>[] valuesRes = new Value<?>[values.length];

            for (int i = 0; i < values.length; i++) {
                valuesRes[i] = Value.newIntegerValue(values[i]);
            }

            return new ValueResponse(ResponseCode.CONTENT, new LwM2mResource(getId(), valuesRes));
        }
    }

    public class StringValueResource extends LwM2mClientResource {

        private String value;

        public StringValueResource(final String initialValue) {
            value = initialValue;
        }

        public void setValue(final String newValue) {
            value = newValue;
            valueChanged(new LwM2mResource(getId(), Value.newStringValue(value)));
        }

        @Override
        public LwM2mResponse write(LwM2mNode node) {
            System.out.println("\tDevice: Writing on Resource " + getId());
            setValue((String) ((LwM2mResource) node).getValue().value);
            return new LwM2mResponse(ResponseCode.CHANGED);
        }

        @Override
        public ValueResponse read() {
            System.out.println("\tDevice: Reading on Resource " + getId());
            return new ValueResponse(ResponseCode.CONTENT, new LwM2mResource(getId(), Value.newStringValue(value)));
        }

    }

    public class IntegerValueResource extends LwM2mClientResource {

        private Integer value;

        public IntegerValueResource(final int initialValue) {
            value = initialValue;
        }

        public void setValue(final Integer newValue) {
            value = newValue;
            valueChanged(new LwM2mResource(getId(), Value.newIntegerValue(value)));
        }

        @Override
        public LwM2mResponse write(LwM2mNode node) {
            System.out.println("\tDevice: Writing on Integer Resource " + getId());
            setValue((Integer) ((LwM2mResource) node).getValue().value);
            return new LwM2mResponse(ResponseCode.CHANGED);
        }

        @Override
        public ValueResponse read() {
            System.out.println("\tDevice: Reading on IntegerResource " + getId());
            return new ValueResponse(ResponseCode.CONTENT, new LwM2mResource(getId(), Value.newIntegerValue(value)));
        }
    }

    public class ExecutableResource extends LwM2mClientResource {

        public ExecutableResource() {
        }

        @Override
        public LwM2mResponse execute() {
            System.out.println("Executing on Resource " + getId());
            return new LwM2mResponse(ResponseCode.CHANGED);
        }
    }
}
