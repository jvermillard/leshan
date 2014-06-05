package leshan.server.lwm2m.message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import leshan.server.lwm2m.client.Client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResourceSpecTest {

    final static String RESOURCE_URI = "3/0/13";
    ResourceSpec spec;
    Client client;

    @Before
    public void setUp() throws Exception {
    }

    private void givenASimpleClient() throws UnknownHostException {
        this.client = new Client("ID", "urn:client", InetAddress.getLocalHost(), 5661, "1.0", 10000L, null, null, null,
                new Date());
    }

    @Test
    public void test() throws IOException {
        givenASimpleClient();
        this.spec = new ResourceSpec(this.client, 3, 0, 13);
        Assert.assertEquals(RESOURCE_URI, this.spec.asRelativePath());
    }

}
