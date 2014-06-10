package leshan.server.lwm2m.message.californium;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import leshan.server.lwm2m.client.Client;

public abstract class BasicTestSupport {

    Client client;
    InetAddress destination;
    int destinationPort = 5000;

    void givenASimpleClient() throws UnknownHostException {
        this.client = new Client("ID", "urn:client", InetAddress.getLocalHost(), this.destinationPort, "1.0", 10000L,
                null, null, null, new Date());
    }


}
