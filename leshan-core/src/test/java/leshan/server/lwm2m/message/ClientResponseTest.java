package leshan.server.lwm2m.message;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClientResponseTest {

    String textPayload = "This is ASCII-Printable TEXT";
    String jsonPayload = "{\"this\": \"is\", \"JSON\": \"text\"}";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testConstructorSetsAllFields() {
        ClientResponse response = new ClientResponse("2.05", this.textPayload.getBytes(), ContentFormat.TEXT.getCode());
        Assert.assertEquals("2.05", response.getCode());
        Assert.assertEquals(ContentFormat.TEXT, response.getFormat());
        Assert.assertArrayEquals(this.textPayload.getBytes(), response.getContent());
    }

    @Test
    public void testConstructorDeterminesContentFormatFromTextPayload() {
        ClientResponse response = new ClientResponse("2.05", this.textPayload.getBytes(), null);
        Assert.assertEquals(ContentFormat.TEXT, response.getFormat());
    }

    @Test
    public void testConstructorDeterminesContentFormatFromJsonPayload() {
        ClientResponse response = new ClientResponse("2.05", this.jsonPayload.getBytes(), null);
        Assert.assertEquals(ContentFormat.JSON, response.getFormat());
    }

}
