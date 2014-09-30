package leshan.client.lwm2m.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import leshan.client.lwm2m.operation.LwM2mCreateExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

import org.junit.Test;

public class LwM2mObjectInstanceTest {

	private static final boolean REQUIRED = true;
	private static final boolean MANDATORY = true;
	private static final boolean SINGLE = true;
	private LwM2mClientObjectDefinition definition;

	@Test
	public void testSingleResource() {
		int resourceId = 12;
		initializeObjectWithSingleResource(resourceId, "hello");

		assertCorrectRead(createInstance(definition, new byte[0]),
				new Tlv(TlvType.RESOURCE_VALUE, null, "hello".getBytes(), resourceId));
	}

	@Test
	public void testMultipleResourceWithOneInstance() {
		int resourceId = 65;
		initializeObjectWithMultipleResource(resourceId,
				Collections.singletonMap(94, "ninety-four".getBytes()));

		assertCorrectRead(createInstance(definition, new byte[0]),
				new Tlv(TlvType.MULTIPLE_RESOURCE, new Tlv[] {
						new Tlv(TlvType.RESOURCE_INSTANCE, null, "ninety-four".getBytes(), 94)
				}, null, resourceId));
	}

	@Test
	public void testMultipleResourceWithThreeInstances() {
		int resourceId = 65;
		Map<Integer, byte[]> values = new HashMap<>();
		values.put(1100, "eleven-hundred".getBytes());
		values.put(10, "ten".getBytes());
		values.put(3, "three".getBytes());
		initializeObjectWithMultipleResource(resourceId,
				values);

		assertCorrectRead(createInstance(definition, new byte[0]),
				new Tlv(TlvType.MULTIPLE_RESOURCE, new Tlv[] {
						new Tlv(TlvType.RESOURCE_INSTANCE, null, "three".getBytes(), 3),
						new Tlv(TlvType.RESOURCE_INSTANCE, null, "ten".getBytes(), 10),
						new Tlv(TlvType.RESOURCE_INSTANCE, null, "eleven-hundred".getBytes(), 1100)
				}, null, resourceId));
	}

	private void initializeObjectWithSingleResource(int resourceId, String value) {
		definition = new LwM2mClientObjectDefinition(100, MANDATORY, SINGLE,
				new SingleResourceDefinition(resourceId, new SampleSingleResource(value), !REQUIRED));
	}

	private void initializeObjectWithMultipleResource(int resourceId, Map<Integer, byte[]> values) {
		definition = new LwM2mClientObjectDefinition(101, MANDATORY, SINGLE,
				new SingleResourceDefinition(resourceId, new SampleMultipleResource(values), !REQUIRED));
	}

	private void assertCorrectRead(LwM2mClientObjectInstance instance, Tlv... tlvs) {
		LwM2mExchange exchange = mock(LwM2mExchange.class);
		instance.handleRead(exchange);
		byte[] bytes = TlvEncoder.encode(tlvs).array();
		verify(exchange).respond(ReadResponse.success(bytes));
	}

	private LwM2mClientObjectInstance createInstance(LwM2mClientObjectDefinition definition, byte[] payload) {
		LwM2mClientObjectInstance instance = new LwM2mClientObjectInstance(0, definition);
		LwM2mCreateExchange createExchange = mock(LwM2mCreateExchange.class);
		when(createExchange.getRequestPayload()).thenReturn(payload);
		instance.handleCreate(createExchange);
		return instance;
	}

	private class SampleSingleResource extends StringLwM2mResource {

		private String value;

		public SampleSingleResource(String value) {
			this.value = value;
		}

		@Override
		public void handleRead(StringLwM2mExchange exchange) {
			exchange.respondContent(value);
		}

	}

	private class SampleMultipleResource extends MultipleLwM2mResource {

		private Map<Integer, byte[]> values;

		public SampleMultipleResource(Map<Integer, byte[]> values) {
			this.values = values;
		}

		@Override
		public void handleRead(MultipleLwM2mExchange exchange) {
			exchange.respondContent(values);
		}

	}

}
