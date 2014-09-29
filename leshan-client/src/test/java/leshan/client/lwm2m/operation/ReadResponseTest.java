package leshan.client.lwm2m.operation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

import org.junit.Test;

public class ReadResponseTest {

	@Test
	public void testEqualityRobustnessForSuccesses() {
		assertEquals(ReadResponse.success("hello".getBytes()), ReadResponse.success("hello".getBytes()));
		assertNotEquals(ReadResponse.success("hello".getBytes()), ReadResponse.success("goodbye".getBytes()));
		assertNotEquals(ReadResponse.success("hello".getBytes()), ReadResponse.failure());
		assertNotEquals(ReadResponse.success("hello".getBytes()), null);
	}

	@Test
	public void testHashCodeRobustnessForSuccesses() {
		assertEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.success("hello".getBytes()).hashCode());
		assertNotEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.success("goodbye".getBytes()).hashCode());
		assertNotEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.failure().hashCode());
	}

	@Test
	public void testEqualityRobustnessForFailures() {
		assertEquals(ReadResponse.failure(), ReadResponse.failure());
		assertNotEquals(ReadResponse.failure(), ReadResponse.success("goodbye".getBytes()));
		assertNotEquals(ReadResponse.failure(), null);
	}

	@Test
	public void testHashCodeRobustnessForFailures() {
		assertEquals(ReadResponse.failure().hashCode(), ReadResponse.failure().hashCode());
		assertNotEquals(ReadResponse.failure(), ReadResponse.success("goodbye".getBytes()).hashCode());
	}

	@Test
	public void testSuccessSinglePayload() {
		ReadResponse response = ReadResponse.success("value".getBytes());
		assertArrayEquals("value".getBytes(), response.getResponsePayload());
	}

	@Test
	public void testSuccessSingleTlv() {
		ReadResponse response = ReadResponse.success("value".getBytes());
		assertEquals(new Tlv(TlvType.RESOURCE_VALUE, null, "value".getBytes(), 0),
				response.getResponsePayloadAsTlv());
	}

	@Test
	public void testSuccessMultiplePayload() {
		Map<Integer, byte[]> readValues = new HashMap<>();
		readValues.put(55, "value".getBytes());
		ReadResponse response = ReadResponse.successMultiple(readValues);
		Tlv[] instances = new Tlv[] {
				new Tlv(TlvType.RESOURCE_INSTANCE, null, "value".getBytes(), 55)
		};
		assertArrayEquals(TlvEncoder.encode(instances).array(), response.getResponsePayload());
	}

	@Test
	public void testSuccessMultipleTlv() {
		Map<Integer, byte[]> readValues = new HashMap<>();
		readValues.put(55, "value".getBytes());
		ReadResponse response = ReadResponse.successMultiple(readValues);
		Tlv[] instances = new Tlv[] {
				new Tlv(TlvType.RESOURCE_INSTANCE, null, "value".getBytes(), 55)
		};
		assertEquals(new Tlv(TlvType.MULTIPLE_RESOURCE, instances, null, 0),
				response.getResponsePayloadAsTlv());
	}

}
