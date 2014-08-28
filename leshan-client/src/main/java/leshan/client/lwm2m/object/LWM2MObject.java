package leshan.client.lwm2m.object;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import leshan.client.lwm2m.object.xsd.LWM2M;
import leshan.client.lwm2m.object.xsd.LWM2MBuilder;

public class LWM2MObject {

	public LWM2MObject(final String schema) {
		this(new ByteArrayInputStream(schema.getBytes()));
	}

	public LWM2MObject(final InputStream stream) {
		final LWM2M obj = LWM2MBuilder.create(stream);
	}

}
